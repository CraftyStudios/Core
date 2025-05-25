package dev.crafty.core.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Wrapper for a {@link ConfigurationSection} that provides a more convenient interface for accessing configuration
 * @since 1.0.1
 */
@Getter
public class SectionWrapper {
    private final ConfigurationSection delegate;

    public SectionWrapper(ConfigurationSection delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate cannot be null");
        }

        this.delegate = delegate;
    }

    public Optional<String> getString(String path) {
        return Optional.ofNullable(delegate.getString(path));
    }

    public Optional<Boolean> getBoolean(String path) {
        return Optional.of(delegate.getBoolean(path));
    }

    public Optional<Integer> getInt(String path) {
        return Optional.of(delegate.getInt(path));
    }

    public Optional<Double> getDouble(String path) {
        return Optional.of(delegate.getDouble(path));
    }

    public Optional<Float> getFloat(String path) {
        return Optional.of(((float) delegate.getDouble(path)));
    }

    public Optional<Long> getLong(String path) {
        return Optional.of(delegate.getLong(path));
    }

    public Optional<List<String>> getStringList(String path) {
        return Optional.of(delegate.getStringList(path));
    }

    public Optional<SectionWrapper> getSection(String path) {
        return Optional.ofNullable(delegate.getConfigurationSection(path))
                .map(SectionWrapper::new);
    }

    public void set(String path, Object value) {
        delegate.set(path, value);
    }

    public boolean contains(String path) {
        return delegate.contains(path);
    }

    public Set<String> getKeys(boolean deep) {
        return delegate.getKeys(deep);
    }

    public SectionWrapper createSection(String path) {
        return new SectionWrapper(delegate.createSection(path));
    }

    public SectionWrapper getOrCreateSection(String path) {
        if (delegate.contains(path)) {
            return new SectionWrapper(delegate.getConfigurationSection(path));
        } else {
            return createSection(path);
        }
    }

    public Optional<Object> get(String path) {
        return Optional.ofNullable(delegate.get(path));
    }

    public Optional<List<?>> getList(String path) {
        return Optional.ofNullable(delegate.getList(path));
    }

    public Optional<List<Integer>> getIntegerList(String path) {
        return Optional.of(delegate.getIntegerList(path));
    }

    public Optional<List<Boolean>> getBooleanList(String path) {
        return Optional.of(delegate.getBooleanList(path));
    }

    public Optional<List<Double>> getDoubleList(String path) {
        return Optional.of(delegate.getDoubleList(path));
    }

    public Optional<List<Float>> getFloatList(String path) {
        return Optional.of(delegate.getFloatList(path));
    }

    public Optional<List<Long>> getLongList(String path) {
        return Optional.of(delegate.getLongList(path));
    }

}
