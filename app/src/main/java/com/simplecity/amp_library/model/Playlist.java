package com.simplecity.amp_library.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.utils.playlists.FavoritesPlaylistManager;
import com.simplecity.amp_library.utils.playlists.PlaylistManager;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import java.io.Serializable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Playlist implements Serializable {


    public @interface Type {
        int PODCAST = 0;
        int RECENTLY_ADDED = 1;
        int MOST_PLAYED = 2;
        int RECENTLY_PLAYED = 3;
        int FAVORITES = 4;
        int USER_CREATED = 5;
    }

    @Type
    public int type;

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String name;
    public boolean canEdit = true;
    public boolean canClear = false;
    public boolean canDelete = true;
    public boolean canRename = true;
    public boolean canSort = true;

    // These are the Playlist rows that we will retrieve.
    protected static final String[] PROJECTION = new String[] {
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME
    };

    public static Query getQuery() {
        return new Query.Builder()
        this.type = type;
        this.setId(id);
        this.name = name;
        this.canEdit = canEdit;
        this.canClear = canClear;
        this.canDelete = canDelete;
        this.canRename = canRename;
        this.canSort = canSort;
        this.type = type;
        this.id = id;
        this.name = name;
        setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID)));
        name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        type = Type.USER_CREATED;
        canClear = true;
        this.canSort = canSort;
    }

    public Playlist(Context context, Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
        name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        type = Type.USER_CREATED;
        canClear = true;

        if (context.getString(R.string.fav_title).equals(name)) {
            type = Type.FAVORITES;
            canDelete = false;
            canRename = false;
        }
    }

    public void clear(PlaylistManager playlistManager, FavoritesPlaylistManager favoritesPlaylistManager) {
                playlistManager.clearPlaylist(getId());
                break;
                favoritesPlaylistManager.clearFavorites();
                break;
            case Playlist.Type.MOST_PLAYED:
                playlistManager.clearMostPlayed();
                break;
            case Playlist.Type.USER_CREATED:
                playlistManager.clearPlaylist(id);
                break;
        return MediaStore.Audio.Playlists.Members.moveItem(context.getContentResolver(), getId(), from, to);
    }

    public void removeSong(@NonNull Song song, PlaylistManager playlistManager, @Nullable Function1<Boolean, Unit> success) {
        playlistManager.removeFromPlaylist(this, song, success);
    }

    public boolean moveSong(Context context, int from, int to) {
        return MediaStore.Audio.Playlists.Members.moveItem(context.getContentResolver(), id, from, to);
    }
        if (getId() != playlist.getId()) return false;
        return name != null ? name.equals(playlist.name) : playlist.name == null;
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
        return name != null ? name.equals(playlist.name) : playlist.name == null;
    }

    @Override
        return "Playlist{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public static Song createSongFromPlaylistCursor(Cursor cursor) {
        Song song = new Song(cursor);
        song.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));
        song.playlistSongId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members._ID));
        song.playlistSongPlayOrder = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER));
        return song;
    }
}
