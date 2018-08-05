import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {RefinedTrackService} from '../core/services/refined-track.service';
import {RefinedAlbum, RefinedArtist, RefinedTrack} from '../core/models';
import {RefinedAlbumService} from '../core/services';
import {RefinedArtistService} from '../core/services/refined-artist.service';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {SafePipe} from './safe-pipe.pipe';


@Component({
  selector: 'app-home-page',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  constructor(
    private router: Router,
    private trackService: RefinedTrackService,
    private albumService: RefinedAlbumService,
    private artistService: RefinedArtistService,
    private safePipe: SafePipe
  ) {
  }
  public currentTrack: RefinedTrack;
  public currentTracksArtists: RefinedArtist[];
  public  currentTracksAlbum: RefinedAlbum;
  public currentUriSafe: SafeResourceUrl;

  ngOnInit() {
   this.getRandomTrack();
  }

  getRandomTrack(): void {
    this.trackService.getRandom()
      .subscribe((refinedTrack) => {
        this.currentTrack = refinedTrack;

        this.albumService.getById(this.currentTrack.albumId)
          .subscribe((refinedAlbum) => {
            this.currentTracksAlbum = refinedAlbum;
          });

        this.artistService.getById(this.currentTrack.artistIds)
          .subscribe((refinedArtists) => {
            this.currentTracksArtists = refinedArtists;
          });

        this.currentUriSafe = 'https://open.spotify.com/embed?uri=' + this.currentTrack.spotifyURI;
      });

  }
}
