package ru.nsu;

import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.google.inject.Inject;

import org.mybatis.guice.transactional.Transactional;

/**
 * Main page for news {@link com.softmotions.ncms.asm.render.AsmController}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NewsMainPageController implements AsmController {

    private final AsmDAO adao;



    @Inject
    public NewsMainPageController(AsmDAO adao) {
        this.adao = adao;
    }

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
        addMainEvents(ctx);
        return false;
    }


    private void addMainEvents(AsmRendererContext ctx) throws Exception {





    }

}
