package com.softmotions.ncms.asm.render;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.Asm;

/**
 * Various helpers.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
@SuppressWarnings("unchecked")
public class AsmRendererHelper {

    private final Injector injector;

    @Inject
    public AsmRendererHelper(Injector injector) {
        this.injector = injector;
    }

    public AsmController createControllerInstance(Asm asm,
                                                  String controllerClassName) throws AsmRenderingException {
        Class controllerClass;
        try {
            ClassLoader cl = MoreObjects.firstNonNull(Thread.currentThread().getContextClassLoader(),
                                                      asm.getClass().getClassLoader());
            controllerClass = cl.loadClass(controllerClassName);
            if (!AsmController.class.isAssignableFrom(controllerClass)) {
                throw new AsmRenderingException("AsmController: '" + controllerClassName + "' " +
                                                "' class does not implement: " + AsmController.class.getName() +
                                                " interface for assembly: " + asm.getName());
            }
        } catch (ClassNotFoundException e) {
            throw new AsmRenderingException("AsmController class: '" + controllerClassName +
                                            "' not found for assembly: " + asm.getName(), e);
        }

        try {
            return (AsmController) injector.getInstance(controllerClass);
        } catch (Exception e) {
            throw new AsmRenderingException("Failed to instantiate controller: '" + controllerClassName +
                                            "' for assembly: " + asm.getName(), e);
        }
    }
}
