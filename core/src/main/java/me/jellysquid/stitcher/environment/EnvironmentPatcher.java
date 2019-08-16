package me.jellysquid.stitcher.environment;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.patcher.ClassPatcher;
import me.jellysquid.stitcher.patcher.ClassPatcherBuilder;
import me.jellysquid.stitcher.plugin.Plugin;
import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;
import me.jellysquid.stitcher.util.FilesHelper;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
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

    private final HashMap<String, List<ClassPatcher>> transformersByClass = new HashMap<>();

    private final List<ClassPatcher> transformers = new ArrayList<>();

    private final ClassPatcherBuilder parser = new ClassPatcherBuilder();

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
        List<ClassPatcher> transformers = this.transformersByClass.get(name);

        if (transformers == null || transformers.isEmpty()) {
            return bytes;
        }

        ClassNode classNode = new ClassNode();

        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES); // We always re-calculate frames, so it's unnecessary to waste CPU time decoding them

        boolean modified = false;

        for (ClassPatcher patcher : transformers) {
            try {
                modified = patcher.transformClass(classNode);
            } catch (TransformerException e) {
                throw new RuntimeException("Failed to transform class using transformer " + patcher, e);
            }
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

    public List<ClassPatcher> registerTransformerGroup(Plugin plugin, PluginGroupConfig config) {
        List<ClassPatcher> patchers = new ArrayList<>();

        for (String transformer : config.getTransformers()) {
            String qualifiedName = config.getPackageRoot() + "." + transformer;
            String bytecodePath = qualifiedName.replace('.', '/') + ".class";

            PluginResource resource = plugin.getResource(bytecodePath);

            ClassPatcher patcher = this.loadClassPatcherFromPlugin(resource);

            patchers.add(patcher);
        }

        return patchers;
    }

    private ClassPatcher loadClassPatcherFromPlugin(PluginResource resource) {
        long start = System.currentTimeMillis();

        ClassPatcher patcher = this.parser.createClassPatcher(resource);

        Stitcher.LOGGER.debug("Loaded class patcher from bytecode {} in {} ({}ms)", resource.getPath(),
                resource.getPlugin(), System.currentTimeMillis() - start);

        try {
            this.addPatcher(patcher);
        } catch (Exception e) {
            throw new RuntimeException("Could not add class patcher to environment: " + resource.getPath(), e);
        }

        return patcher;
    }

    private void addPatcher(ClassPatcher patcher) {
        String target = patcher.getTarget().getClassName();

        if (this.environment.isClassLoaded(target)) {
            throw new RuntimeException("The class " + target + " has already been loaded in this environment");
        }

        this.transformersByClass.computeIfAbsent(target, (key) -> new ArrayList<>())
                .add(patcher);
        this.transformers.add(patcher);
    }

    public Collection<ClassPatcher> getRegisteredPatchers() {
        return Collections.unmodifiableCollection(this.transformers);
    }
}
