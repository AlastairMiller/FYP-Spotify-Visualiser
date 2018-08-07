import {Component, OnInit} from '@angular/core';
import {RefinedArtist} from '../../core/models';

@Component({
  selector: 'app-true-list-page',
  templateUrl: './true-list.component.html'
})
export class TrueListComponent implements OnInit {
  constructor() {

  }

  private currentlyShowing;
  public columnNames = [];

  ngOnInit() {
    this.currentlyShowing = 'RefinedArtist';
    this.columnNames = this.determineColumnNames();
  }

  private determineColumnNames() {
    switch (this.currentlyShowing) {
      case 'RefinedArtist':
        return ['Name', 'Genres', 'Followers', 'Popularity'];
      case 'RefinedTrack':
        return ['Name', 'Artist', 'Album(s)', 'Popularity', 'Duration'];
      case 'RefinedAlbum':
        return ['Name', 'Artist', 'Release Date', 'Popularity'];
      case 'RefinedPlaylist':
        return ['Name', 'Country', 'User', 'Number of Followers'];

    }
  }


}
