import {Component, OnInit} from '@angular/core';
import {RefinedArtist} from '../../core/models';

@Component({
  selector: 'app-true-list-page',
  templateUrl: './true-list.component.html'
})
export class TrueListComponent implements OnInit {
  constructor() {

  }

  public currentlyShowing;
  public columnNames = [];
  public rowObjects = [];

  ngOnInit() {
    this.currentlyShowing = 'RefinedAlbum';
    this.determineColumnNames();
  }

  changeColumn(newName: string) {
    this.currentlyShowing = newName;
    this.determineColumnNames();
  }

  determineColumnNames() {
    switch (this.currentlyShowing) {
      case 'RefinedArtist':
        this.columnNames = ['Name', 'Genres', 'Followers', 'Popularity'];
        return;
      case 'RefinedAlbum':
        this.columnNames = ['Name', 'Artist', 'Release Date', 'Popularity'];
        return;
      case 'RefinedPlaylist':
        this.columnNames = ['Name', 'Country', 'User', 'Number of Followers'];
        return;
      case 'RefinedTrack':
        this.columnNames = ['Name', 'Artist', 'Album(s)', 'Popularity', 'Duration'];
        return;
    }
  }

  getRows(){
    switch (this.currentlyShowing) {
      case 'RefinedArtist':
        return;
      case 'RefinedAlbum':
        return;
      case 'RefinedPlaylist':
        return;
      case 'RefinedTrack':
        return;
    }
  }

}
