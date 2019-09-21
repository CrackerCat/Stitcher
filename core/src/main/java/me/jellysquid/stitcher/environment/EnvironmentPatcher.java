package me.jellysquid.stitcher.environment;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.patcher.ClassPatcher;
import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.patcher.TransformationData;
import me.jellysquid.stitcher.patcher.TransformationReader;
import me.jellysquid.stitcher.plugin.Plugin;
import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;
import me.jellysquid.stitcher.util.FilesHelper;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnvironmentPatcher {
    private final ExecutorService executor;

    private final HashMap<String, ClassPatcher> patchersByClass = new HashMap<>();

    private final List<ClassTransformer> transformers = new ArrayList<>();

    private final TransformationReader parser = new TransformationReader();

    private final Path debugDirectory;

    private final Environment environment;

    private final boolean debug;

    public EnvironmentPatcher(Environment environment, boolean debug) {
        this.environment = environment;

        this.debugDirectory = environment.getHomeDirectory().resolve(".stitcher/classes");
        this.debug = debug;
        this.executor = debug ? Executors.newSingleThreadExecutor() : null;

        this.prepare();
    }

    private void prepare() {
        if (this.debug) {
            try {
                FilesHelper.recursiveDeleteDirectory(this.debugDirectory);

                Files.createDirectories(this.debugDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Failed to cleanup debug directory", e);
            }
        }
    }

    public final byte[] transform(String name, byte[] bytes) {
        ClassPatcher patcher = this.patchersByClass.get(name);

        if (patcher == null) {
            return bytes;
        }

        ClassNode classNode = new ClassNode();

        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES); // We always re-calculate frames, so it's unnecessary to waste CPU time decoding them

        final boolean modified;

        try {
            modified = patcher.transformClass(classNode);
        } catch (TransformerException e) {
            throw new RuntimeException(String.format("Failed to transform class using transformer %s", patcher), e);
        }

        if (modified) {
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);

            byte[] result = classWriter.toByteArray();

            if (this.debug) {
                this.executor.submit(() -> {
                    try {
                        this.saveClassBytes(name, result);
                    } catch (IOException e) {
                        Stitcher.LOGGER.warn("Failed to save transformed class bytecode", e);
                    }
                });
            }

            return result;
        }

        return bytes;
    }

    private void saveClassBytes(String name, byte[] bytes) throws IOException {
        Path path = this.debugDirectory.resolve(name.replace('.', '/') + ".class");

        Files.createDirectories(path.getParent());

        try (FileOutputStream out = new FileOutputStream(path.toFile())) {
            out.write(bytes);
        }
    }

    public List<ClassTransformer> registerTransformerGroup(Plugin plugin, PluginGroupConfig config) {
        List<ClassTransformer> transformers = new ArrayList<>();

        for (String transformer : config.getTransformers()) {
            String qualifiedName = String.format("%s.%s", config.getPackageRoot(), transformer);
            String bytecodePath = String.format("%s.class", qualifiedName.replace('.', '/'));

            PluginResource resource = plugin.getResource(bytecodePath);

            transformers.addAll(this.loadClassPatcherFromPlugin(resource));
        }

        return transformers;
    }

    private List<ClassTransformer> loadClassPatcherFromPlugin(PluginResource resource) {
        long start = System.currentTimeMillis();

        TransformationData data = this.parser.readTransformations(resource);

        Stitcher.LOGGER.debug("Loaded transformations from {} in {} ({}ms)", resource.getPath(),
                resource.getPlugin(), System.currentTimeMillis() - start);

        try {
            ClassPatcher patcher = this.getPatcher(data.getTarget());
            patcher.addTransformers(data.getTransformers());
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not add class patcher to environment: %s", resource.getPath()), e);
        }

        this.transformers.addAll(data.getTransformers());

        return data.getTransformers();
    }

    private ClassPatcher getPatcher(Type type) {
        ClassPatcher patcher = this.patchersByClass.get(type.getClassName());

        if (patcher == null) {
            patcher = this.createPatcher(type);
        }

        return patcher;
    }

    private ClassPatcher createPatcher(Type type) {
        ClassPatcher patcher = new ClassPatcher(type);

        if (this.environment.isClassLoaded(type.getClassName())) {
            throw new RuntimeException(String.format("The class %s has already been loaded in this environment", type.getClassName()));
        }

        this.patchersByClass.put(type.getClassName(), patcher);

        return patcher;
    }

    public Collection<ClassTransformer> getRegisteredPatchers() {
        return Collections.unmodifiableCollection(this.transformers);
    }
}
