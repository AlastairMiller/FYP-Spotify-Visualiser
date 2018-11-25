import {Component} from '@angular/core';
import {RefinedAlbumService, RefinedArtistService, RefinedPlaylistService, RefinedTrackService} from '../../core/services';

@Component({
  selector: 'app-search-page',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})

export class SearchComponent {

  public searchChoice = 'Album';
  public searchTerm = '';
  public searchResults;

  constructor(private albumService: RefinedAlbumService,
              private artistService: RefinedArtistService,
              private playlistService: RefinedPlaylistService,
              private trackService: RefinedTrackService) {

  }

  search() {
    switch (this.searchChoice) {
      case 'Album':
        this.albumService.search(this.searchTerm, 0, 5)
          .subscribe((refinedAlbums) => {
            this.searchResults = refinedAlbums;
          });
        break;
      case 'Artist':
        this.searchResults = this.artistService.search(this.searchTerm, 0, 5)
          .subscribe((refinedArtists) => {
            this.searchResults = refinedArtists;
          });
        break;
      case  'Playlist':
        this.searchResults = this.playlistService.search(this.searchTerm, 0, 5)
          .subscribe((refinedPlaylists) => {
            this.searchResults = refinedPlaylists;
          });
        break;
      case 'Track':
        this.searchResults = this.trackService.search(this.searchTerm, 0, 5)
          .subscribe((refinedTracks) => {
            this.searchResults = refinedTracks;
          });
        break;
      default:
        console.log('Invalid search choice');
        break;
    }
  }


  dropdownedClicked(choice: any) {

    switch (choice) {
      case 'Album':
        this.searchChoice = 'Album';
        break;
      case 'Artist':
        this.searchChoice = 'Artist';
        break;
      case 'Playlist':
        this.searchChoice = 'Playlist';
        break;
      case 'Track':
        this.searchChoice = 'Track';
        break;
      default:
        console.log('Error: Invalid selection');
    }
    console.log(this.searchResults);

  }
}
