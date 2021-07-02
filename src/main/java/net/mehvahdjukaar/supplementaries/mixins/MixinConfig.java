package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraftforge.common.ForgeConfigSpec;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MixinConfig implements IMixinConfigPlugin {

    public static List<String> getMixinClassesNames() {
        try {
            String className = MixinConfig.class.getName();
            String packageName = MixinConfig.class.getPackage().getName();
            return getClassesInPackage(packageName).stream()
                    .filter(s->!s.equals(className)).map(s->s.substring(packageName.length() + 1))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalStateException("Could not fetch mixin classes, giving up: " + e.getMessage());
        }
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @see <a href="https://stackoverflow.com/a/520344">Source</a>
     * @param packageName The base package
     * @return fully qualified class name strings
     */
    private static List<String> getClassesInPackage(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<String> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @see <a href="https://stackoverflow.com/a/520344">Source</a>
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return fully qualified class name strings
     */
    private static List<String> findClasses(File directory, String packageName) {
        List<String> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
            }
        }
        return classes;
    }


    @Override
    public void onLoad(String mixinPackage) {
        try {
            RegistryConfigs.createSpec();
        }catch (Exception exception){
            throw new RuntimeException("Failed to create registry configs: "+ exception);
        }

        try {
            RegistryConfigs.load();
        }catch (Exception exception){
            throw new RuntimeException("Failed to load config supplementaries-registry.toml. Try deleting it");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String truncatedName = mixinClassName.substring(this.getClass().getPackage().getName().length() + 1);
        ForgeConfigSpec.BooleanValue config = RegistryConfigs.reg.MIXIN_VALUES.get(truncatedName);
        return config == null || config.get();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}