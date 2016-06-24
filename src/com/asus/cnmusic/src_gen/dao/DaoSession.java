package com.asus.cnmusic.src_gen.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.asus.cnmusic.src_gen.HistoryMusicNote;
import com.asus.cnmusic.src_gen.FavoriteMusicNote;

import com.asus.cnmusic.src_gen.dao.HistoryMusicNoteDao;
import com.asus.cnmusic.src_gen.dao.FavoriteMusicNoteDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig historyMusicNoteDaoConfig;
    private final DaoConfig favoriteMusicNoteDaoConfig;

    private final HistoryMusicNoteDao historyMusicNoteDao;
    private final FavoriteMusicNoteDao favoriteMusicNoteDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        historyMusicNoteDaoConfig = daoConfigMap.get(HistoryMusicNoteDao.class).clone();
        historyMusicNoteDaoConfig.initIdentityScope(type);

        favoriteMusicNoteDaoConfig = daoConfigMap.get(FavoriteMusicNoteDao.class).clone();
        favoriteMusicNoteDaoConfig.initIdentityScope(type);

        historyMusicNoteDao = new HistoryMusicNoteDao(historyMusicNoteDaoConfig, this);
        favoriteMusicNoteDao = new FavoriteMusicNoteDao(favoriteMusicNoteDaoConfig, this);

        registerDao(HistoryMusicNote.class, historyMusicNoteDao);
        registerDao(FavoriteMusicNote.class, favoriteMusicNoteDao);
    }
    
    public void clear() {
        historyMusicNoteDaoConfig.getIdentityScope().clear();
        favoriteMusicNoteDaoConfig.getIdentityScope().clear();
    }

    public HistoryMusicNoteDao getHistoryMusicNoteDao() {
        return historyMusicNoteDao;
    }

    public FavoriteMusicNoteDao getFavoriteMusicNoteDao() {
        return favoriteMusicNoteDao;
    }

}