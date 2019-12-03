package com.amitinside.featureflags.provider;

import static java.util.stream.Collectors.toList;
import static org.apache.felix.service.command.CommandProcessor.COMMAND_FUNCTION;
import static org.apache.felix.service.command.CommandProcessor.COMMAND_SCOPE;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amitinside.featureflags.api.FeatureDTO;
import com.amitinside.featureflags.api.FeatureManager;

//@formatter:off
@Component(
         service = Object.class,
         property = {
              COMMAND_SCOPE + "=featureflags",
              COMMAND_FUNCTION + "=features",
              COMMAND_FUNCTION + "=updatefeature",
              COMMAND_FUNCTION + "=enablefeature",
              COMMAND_FUNCTION + "=disablefeature"
         }
)
//@formatter:on
public final class FeatureFlagsGogoCommand {

    @Reference
    private FeatureManager featureManager;

    public List<FeatureDTO> features() {
        return featureManager.getFeatures().collect(toList());
    }

    public void updatefeature(String featureID, boolean isEnabled) {
        featureManager.updateFeature(featureID, isEnabled);
    }

    public void enablefeature(String featureID) {
        updatefeature(featureID, true);
    }

    public void disablefeature(String featureID) {
        updatefeature(featureID, false);
    }
}
