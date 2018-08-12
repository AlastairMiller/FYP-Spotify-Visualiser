import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {RefinedAlbum, RefinedArtist} from '../../core/models';
import {
  RefinedAlbumService,
  RefinedArtistService,
  RefinedPlaylistService,
  RefinedTrackService
} from '../../core/services';

@Component({
  selector: 'app-true-list-page',
  templateUrl: './true-list.component.html',
  styleUrls: ['./true-list.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrueListComponent implements OnInit, OnDestroy {
  constructor(private albumService: RefinedAlbumService,
              private artistService: RefinedArtistService,
              private playlistService: RefinedPlaylistService,
              private trackService: RefinedTrackService,
              private ref: ChangeDetectorRef) {
    this.counter = setInterval(() => {
      this.numberOfTicks++;
      this.ref.markForCheck();
      console.log('updating view');
    }, 100);
  }

  public currentlyShowing;
  public columnNames = [];
  public rowsObject: any = Array<any>();
  private numberOfTicks = 0;
  private readonly counter;
  public rowPointer = 1;

  ngOnInit() {
    this.changeColumn('RefinedAlbum');
  }

  ngOnDestroy() {
    clearInterval(this.counter);
  }

  changeColumn(newName: string) {
    this.rowPointer = 1;
    this.currentlyShowing = newName;
    this.determineColumnNames();
    this.getRows();
    console.log(this.rowsObject);
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

  getRows() {
    switch (this.currentlyShowing) {
      case 'RefinedArtist':
        return this.artistService.getMostPopular(this.rowPointer - 1, this.rowPointer + 24)
          .subscribe((refinedArtist) => {
            this.rowsObject = refinedArtist;
          });
      case 'RefinedAlbum':
        return this.albumService.getMostPopular(this.rowPointer - 1, this.rowPointer + 24)
          .subscribe((refinedAlbums) => {
            this.rowsObject = refinedAlbums;
            this.changeArtistIdsToNames();
          });
      case 'RefinedPlaylist':
        return this.playlistService.getMostPopular(this.rowPointer - 1, this.rowPointer + 24)
          .subscribe((refinedPlaylists) => {
            this.rowsObject = refinedPlaylists;
          });
      case 'RefinedTrack':
        return this.trackService.getMostPopular(this.rowPointer - 1, this.rowPointer + 24)
          .subscribe((refinedTracks) => {
            this.rowsObject = refinedTracks;
            this.changeArtistIdsToNames();
            this.changeAlbumsIdsToNames();
          });
    }
  }


  changeAlbumsIdsToNames() {
    let allAlbumsIds: any = [this.rowsObject.length];
    for (const n of Object.keys(this.rowsObject)) {
      allAlbumsIds[n] = this.rowsObject[n].albumId;
    }
    allAlbumsIds = allAlbumsIds.flat();
    this.albumService.getByIds(allAlbumsIds)
      .subscribe(albumNamesAsync => {
        for (const i of Object.keys(this.rowsObject)) {
          for (const n in albumNamesAsync) {
            if (this.rowsObject[i].albumId === albumNamesAsync[n].id) {
              this.rowsObject[i].albumId = albumNamesAsync[n].name;
              console.log(this.rowsObject[i].name + this.rowsObject[i].albumId);
            }
          }
        }
      });
  }

  changeArtistIdsToNames() {
    if (this.currentlyShowing == 'RefinedAlbum') {
      let allArtistIds: any = [this.rowsObject.length];
      for (const n of Object.keys(this.rowsObject)) {
        allArtistIds[n] = this.rowsObject[n].artistsIds;
      }
      allArtistIds = allArtistIds.flat();
      this.artistService.getByIds(allArtistIds)
        .subscribe(artistNamesAsync => {
            for (const i of Object.keys(this.rowsObject)) {
              for (const n of Object.keys(this.rowsObject[i].artistsIds)) {
                for (const p in artistNamesAsync) {
                  if (this.rowsObject[i].artistsIds[n] === artistNamesAsync[p].id) {
                    this.rowsObject[i].artistsIds[n] = artistNamesAsync[p].name;
                    console.log(this.rowsObject[i].name + this.rowsObject[i].artistsIds[n]);

                  }
                }
              }
            }
          }
        );
    } else {
      let allArtistIds: any = [this.rowsObject.length];
      for (const n of Object.keys(this.rowsObject)) {
        allArtistIds[n] = this.rowsObject[n].artistIds;
      }
      allArtistIds = allArtistIds.flat();
      this.artistService.getByIds(allArtistIds)
        .subscribe(artistNamesAsync => {
            for (const i of Object.keys(this.rowsObject)) {
              for (const n of Object.keys(this.rowsObject[i].artistIds)) {
                for (const p in artistNamesAsync) {
                  if (this.rowsObject[i].artistIds[n] === artistNamesAsync[p].id) {
                    this.rowsObject[i].artistIds[n] = artistNamesAsync[p].name;
                    console.log(this.rowsObject[i].name + this.rowsObject[i].artistIds[n]);

                  }
                }
              }
            }
          }
        );
    }
  }

}
