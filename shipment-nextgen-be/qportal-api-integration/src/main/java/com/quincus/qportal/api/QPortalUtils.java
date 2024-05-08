package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.NONE)
@UtilityClass
public class QPortalUtils {

    public static String lookupLocationIdFromName(String locationName, List<String> ancestors,
                                                  @NonNull List<QPortalLocation> refLocationList) {
        if (locationName == null) {
            return null;
        }

        String locationAncestor = CollectionUtils.isEmpty(ancestors) ? "" : String.join(", ", ancestors);

        return refLocationList.stream()
                .filter(QPortalLocation::isActive)
                .filter(loc -> loc.getName().equalsIgnoreCase(locationName))
                .filter(loc -> loc.getAncestors().equalsIgnoreCase(locationAncestor))
                .map(QPortalLocation::getId)
                .findFirst().orElse(null);
    }

    public static <T extends QPortalModel> String lookupIdFromName(String name, @NonNull List<T> refList) {
        if (name == null) {
            return null;
        }

        return refList.stream().filter(x -> name.equalsIgnoreCase(x.getName()))
                .map(QPortalModel::getId)
                .findFirst().orElse(null);
    }
}
