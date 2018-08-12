import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {RefinedTrackService} from '../core/services/refined-track.service';
import {RefinedAlbum, RefinedArtist, RefinedTrack} from '../core/models';
import {RefinedAlbumService} from '../core/services';
import {RefinedArtistService} from '../core/services/refined-artist.service';
import {SafeResourceUrl} from '@angular/platform-browser';
import {interval} from 'rxjs/internal/observable/interval';


@Component({
  selector: 'app-home-page',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  constructor(
    private router: Router,
    private trackService: RefinedTrackService,
    private albumService: RefinedAlbumService,
    private artistService: RefinedArtistService) {
  }

  public currentTrack: RefinedTrack;
  public currentTracksArtists: RefinedArtist[];
  public currentTracksAlbum: RefinedAlbum;
  public currentUriSafe: SafeResourceUrl;
  public currentTracksCountryCode: String;
  private timedSubscription;
  private isGetFinished: boolean;

  ngOnInit() {
    this.isGetFinished = true;
    this.getRandomTrack();
    this.timedSubscription = interval(10000)
      .subscribe((t) => this.getRandomTrack());
  }

  ngOnDestroy() {
    this.timedSubscription.unsubscribe();
  }

  getRandomTrack(): void {
    if (this.isGetFinished) {
      this.isGetFinished = false;
      this.trackService.getRandom()
        .subscribe((refinedTrack) => {
          this.currentTrack = refinedTrack;

          this.currentUriSafe = 'https://open.spotify.com/embed?uri=' + this.currentTrack.spotifyURI;

          this.albumService.getById(this.currentTrack.albumId)
            .subscribe((refinedAlbum) => {
              this.currentTracksAlbum = refinedAlbum;
            });

          this.artistService.getByIds(this.currentTrack.artistIds)
            .subscribe((refinedArtists) => {
              this.currentTracksArtists = refinedArtists;
            });

          this.trackService.getMostPopularCountry(refinedTrack.id)
            .subscribe((countryCode) => {
              this.currentTracksCountryCode = countryCode;
              this.isGetFinished = true;
            });

        });
    }
  }


}
