package ru.nsu.hotfixes;

import com.google.inject.Inject;
import com.softmotions.ncms.update.HotFix;
import com.softmotions.weboot.mb.MBDAOSupport;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Tyutyunkov VE (tyutyunkov@gmail.com)
 */
public class NavCachedPathFixes extends MBDAOSupport implements HotFix {

    @Inject
    public NavCachedPathFixes(SqlSession sess) {
        super(NavCachedPathFixes.class.getPackage().getName(), sess);
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public void apply() throws Exception {
        update("NavCachedPathFixes:prepare");
        while (update("NavCachedPathFixes:update") > 0) ;
    }
}
