package youyihj.zenutils.impl.mixin.crafttweaker;

import org.spongepowered.asm.mixin.Mixin;
import stanhebben.zenscript.compiler.EnvironmentScript;
import youyihj.zenutils.impl.mixin.itf.IMixinTargetEnvironment;

import java.util.Collections;
import java.util.List;

/**
 * @author youyihj
 */
@Mixin(EnvironmentScript.class)
public class MixinEnvironmentScript implements IMixinTargetEnvironment {
    private List<String> targets;

    @Override
    public List<String> getTargets() {
        return targets == null ? Collections.emptyList() : targets;
    }

    @Override
    public void setTargets(List<String> targets) {
        this.targets = targets;
    }
}