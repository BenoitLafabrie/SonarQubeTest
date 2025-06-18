package com.simplecity.amp_library.model;

import java.util.ArrayList;

public class Suggestion {

    private AlbumArtist mostPlayedArtist;

    public AlbumArtist getMostPlayedArtist() {
        return mostPlayedArtist;
    }

    public void setMostPlayedArtist(AlbumArtist mostPlayedArtist) {
        this.mostPlayedArtist = mostPlayedArtist;
    }
    public Album mostPlayedAlbum;
    public Song mostPlayedSong;
    public ArrayList<Song> favouriteSongsOne = new ArrayList<>(3);
    public ArrayList<Song> favouriteSongsTwo = new ArrayList<>(3);
    public ArrayList<Album> recentlyPlayedAlbums = new ArrayList<>(4);
    public ArrayList<Album> recentlyAddedAlbumsOne = new ArrayList<>(2);
    public ArrayList<Album> recentlyAddedAlbumsTwo = new ArrayList<>(2);

    public static class Favourites {
        public ArrayList<Song> songsOne;
        public ArrayList<Song> songsTwo;

        public Favourites(ArrayList<Song> songsOne, ArrayList<Song> songsTwo) {
            this.songsOne = songsOne;
            this.songsTwo = songsTwo;
        }
    }

    public static class AlbumsGroup {
        public ArrayList<Album> recentlyPlayed;
        public ArrayList<Album> recentlyAddedOne;
        public ArrayList<Album> recentlyAddedTwo;

        public AlbumsGroup(ArrayList<Album> recentlyPlayed, ArrayList<Album> recentlyAddedOne, ArrayList<Album> recentlyAddedTwo) {
            this.recentlyPlayed = recentlyPlayed;
            this.recentlyAddedOne = recentlyAddedOne;
            this.recentlyAddedTwo = recentlyAddedTwo;
        }
        this.setMostPlayedArtist(mostPlayedAlbumArtist);

    public Suggestion(AlbumArtist mostPlayedAlbumArtist,
                      Album mostPlayedAlbum,
                      Song mostPlayedSong,
                      Favourites favourites,
                      AlbumsGroup albumsGroup) {

        this.mostPlayedArtist = mostPlayedAlbumArtist;
        this.mostPlayedAlbum = mostPlayedAlbum;
        this.mostPlayedSong = mostPlayedSong;
        this.favouriteSongsOne = favourites.songsOne;
        this.favouriteSongsTwo = favourites.songsTwo;
                "mostPlayedArtist=" + getMostPlayedArtist() +
        this.recentlyAddedAlbumsOne = albumsGroup.recentlyAddedOne;
        this.recentlyAddedAlbumsTwo = albumsGroup.recentlyAddedTwo;
    }

    @Override
    public String toString() {
        return "Suggestion{" +
                "mostPlayedArtist=" + mostPlayedArtist +
                ", mostPlayedAlbum=" + mostPlayedAlbum +
                ", mostPlayedSong=" + mostPlayedSong +
                ", favouriteSongsOne=" + favouriteSongsOne +
                ", favouriteSongsTwo=" + favouriteSongsTwo +
                ", recentlyPlayedAlbums=" + recentlyPlayedAlbums +
                ", recentlyAddedAlbumsOne=" + recentlyAddedAlbumsOne +
                ", recentlyAddedAlbumsTwo=" + recentlyAddedAlbumsTwo +
                '}';
    }
}

