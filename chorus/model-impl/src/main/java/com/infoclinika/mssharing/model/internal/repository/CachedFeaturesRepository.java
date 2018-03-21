package com.infoclinika.mssharing.model.internal.repository;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.infoclinika.mssharing.model.internal.entity.Feature;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Herman Zamula
 */
@Repository("cachedFeaturesRepository")
@Transactional(readOnly = true)
public class CachedFeaturesRepository extends FeaturesRepository {

    private final LoadingCache<LabFeatureItem, Boolean> enabledPerLabCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<LabFeatureItem, Boolean>() {
                @Override
                @ParametersAreNonnullByDefault
                public Boolean load(LabFeatureItem item) throws Exception {
                    return enabledForLabFromDB(item.featureName, item.lab);
                }
            });

    private final LoadingCache<Optional<Void>, Map<String, Feature>> featuresMapCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<Optional<Void>, Map<String, Feature>>() {
                @Override
                @ParametersAreNonnullByDefault
                public Map<String, Feature> load(Optional<Void> key) throws Exception {
                    List<Feature> features = em.createQuery("from Feature", Feature.class).getResultList();
                    Map<String, Feature> result = new HashMap<>();
                    for (Feature feature : features) {
                        result.put(feature.getName(), feature);
                    }
                    return result;
                }
            });

    @Override
    public Map<String, Feature> get() {
        return featuresMapCache.getUnchecked(Optional.<Void>absent());
    }

    @Override
    public boolean enabledForLab(String name, long lab) {
        return enabledPerLabCache.getUnchecked(new LabFeatureItem(lab, name));
    }

    private boolean enabledForLabFromDB(String name, long lab) {
        return super.enabledForLab(name, lab);
    }

    private static class LabFeatureItem {
        public final long lab;
        public final String featureName;

        private LabFeatureItem(long lab, String featureName) {
            this.lab = lab;
            this.featureName = featureName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LabFeatureItem that = (LabFeatureItem) o;

            if (lab != that.lab) return false;
            if (!featureName.equals(that.featureName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (lab ^ (lab >>> 32));
            result = 31 * result + featureName.hashCode();
            return result;
        }
    }
}
