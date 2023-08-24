package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ModDepHack implements ModDependency {

    private final Kind kind;
    private final Collection<VersionPredicate> ranges;
    private final String modId;

    public ModDepHack(String modId, Kind kind, List<String> matcherString) throws VersionParsingException {
        this.ranges =  VersionPredicate.parse(matcherString);
        this.kind = kind;
        this.modId = modId;
    }


    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getModId() {
        return modId;
    }

    @Override
    public boolean matches(Version version) {
        for (VersionPredicate predicate : ranges) {
            if (predicate.test(version)) return true;
        }

        return false;
    }

    @Override
    public Collection<VersionPredicate> getVersionRequirements() {
        return ranges;
    }

    @Override
    public List<VersionInterval> getVersionIntervals() {
        List<VersionInterval> ret = Collections.emptyList();

        for (VersionPredicate predicate : ranges) {
            ret = VersionInterval.or(ret, predicate.getInterval());
        }

        return ret;
    }
}
