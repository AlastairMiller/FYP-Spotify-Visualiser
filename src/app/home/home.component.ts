import {Component, Input, OnChanges, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {RefinedTrackService} from '../core/services/refined-track.service';
import {RefinedAlbum, RefinedArtist, RefinedTrack} from '../core/models';
import {RefinedAlbumService} from '../core/services';
import {RefinedArtistService} from '../core/services/refined-artist.service';
import {SafeResourceUrl} from '@angular/platform-browser';
import {interval} from 'rxjs/internal/observable/interval';
import {AgWordCloudData, AgWordCloudDirective} from 'angular4-word-cloud';
import {CountryCodeToNamePipe} from '../core/pipes/country-code-to-name.pipe';
import {animate, state, style, transition, trigger} from '@angular/animations';


@Component({
  selector: 'app-home-page',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  animations: [
    trigger('visibilityChanged', [
      state('shown', style({opacity: 1})),
      state('hidden', style({opacity: 0})),
      transition('* => *', animate('2s')),
    ])
  ]
})
export class HomeComponent implements OnInit, OnDestroy {
  constructor(
    private router: Router,
    private trackService: RefinedTrackService,
    private albumService: RefinedAlbumService,
    private artistService: RefinedArtistService,
    private codeToName: CountryCodeToNamePipe) {
  }

  public currentTrack: RefinedTrack;
  public currentTracksArtists: RefinedArtist[];
  public currentTracksAlbum: RefinedAlbum;
  public currentUriSafe: SafeResourceUrl;
  public currentTracksCountryCode: String;
  public wordData: Array<AgWordCloudData>;
  public options;
  private isFirstRun;
  @Input() fadeState;
  visibility = 'hidden';
  private timedSubscription;
  @ViewChild('word_cloud_chart') word_cloud_chart: AgWordCloudDirective;

  ngOnInit() {
    this.options = {
      settings: {
        minFontSize: 980,
        maxFontSize: 1000,
      },
      margin: {
        top: 0,
        right: 0,
        bottom: 0,
        left: 0
      },
      labels: false
    };
    this.isFirstRun = true;
    console.log(this.visibility);
    this.getRandomTrack();
    this.timedSubscription = interval(10000)
      .subscribe((t) => this.fadeTranstion());
  }

  ngOnDestroy() {
    this.timedSubscription.unsubscribe();
  }

  fadeTranstion(): void {
    this.fadeState = false;
    this.visibility = 'hidden';
    console.log(this.visibility);
      window.setTimeout(() => {this.getRandomTrack()}, 2000);
  }

  getRandomTrack(): void {
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
            this.constructWordMap();
            this.fadeState = true;
            this.visibility = 'shown';
            console.log(this.visibility);
          });

      });

  }

  constructWordMap(): void {
    const currentTracksArtistsMapData: Array<AgWordCloudData> = [];
    for (const i of this.currentTracksArtists) {
      currentTracksArtistsMapData.push(
        {size: 999, text: 'Artist: ' + i.name, color: 'white'}
      );
    }

    if (this.currentTracksCountryCode === 'UNDEFINED') {
      this.word_cloud_chart.wordData = [
        {size: 998, text: ''},
        {size: 999, text: this.currentTrack.name, color: 'white'},
        {
          size: 999,
            text: 'Length: ' + new Date(this.currentTrack.durationMs).getMinutes() + ':' + this.addZero(new Date(this.currentTrack.durationMs).getSeconds()),
          color: 'white'
        },
        {size: 999, text: 'Appears On: ' + this.currentTracksAlbum.name.toString(), color: 'white'},
        {size: 999, text: 'Popularity: ' + this.currentTrack.popularity + '%', color: 'white'},
      ];
      Array.prototype.push.apply(this.word_cloud_chart.wordData, currentTracksArtistsMapData);
    } else {
      this.word_cloud_chart.wordData = [
        {size: 998, text: ''},
        {size: 999, text: this.currentTrack.name, color: 'white'},
        {
          size: 999,
          text: 'Length: ' + new Date(this.currentTrack.durationMs).getMinutes() + ':' + this.addZero(new Date(this.currentTrack.durationMs).getSeconds()),
          color: 'white'
        },
        {size: 999, text: 'Appears On: ' + this.currentTracksAlbum.name.toString(), color: 'white'},
        {size: 999, text: 'Popularity: ' + this.currentTrack.popularity + '%', color: 'white'},
        {size: 999, text: 'Most Popular In: ' + this.codeToName.transform(this.currentTracksCountryCode), color: 'white'},
      ];
      Array.prototype.push.apply(this.word_cloud_chart.wordData, currentTracksArtistsMapData);
    }
    this.word_cloud_chart.update();
  }

   addZero(i) {
    if (i < 10) {
      i = '0' + i;
    }
    return i;
  }

}

