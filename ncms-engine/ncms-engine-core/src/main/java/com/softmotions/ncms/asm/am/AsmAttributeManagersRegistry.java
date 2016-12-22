package com.softmotions.ncms.asm.am;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface AsmAttributeManagersRegistry {

    @Nullable
    <T extends AsmAttributeManager> T getByType(String type);

    @Nonnull
    Collection<AsmAttributeManager> getAll();

}
