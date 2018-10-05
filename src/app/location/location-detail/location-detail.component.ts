import {Component, OnInit} from '@angular/core';
import {RefinedPlaylistService} from '../../core/services';
import {RefinedPlaylist, RefinedTrack} from '../../core/models';
import {ActivatedRoute} from '@angular/router';
import {CountryNameToCodePipe} from '../../core/pipes/country-name-to-code.pipe';


@Component({
  selector: 'app-list-detail-page',
  templateUrl: './location-detail.component.html',
  styleUrls: ['./location-detail.component.css']
})
export class LocationDetailComponent implements OnInit {

  currentCountry: string;
  currentCountryCode: string;
  currentCountryPlaylists: RefinedPlaylist[];
  private mostPopularSongsHere: RefinedTrack[];
  private mostPopularGenresHere: string[];

  constructor(private playlistService: RefinedPlaylistService,
              private route: ActivatedRoute,
              private countryToCodePipe: CountryNameToCodePipe) {
  }

  ngOnInit(): void {
    this.currentCountry = this.route.snapshot.paramMap.get('locationName');
    this.currentCountryCode = this.countryToCodePipe.transform(this.currentCountry);
    this.playlistService.getPlaylistLocation( this.currentCountryCode, 0, 25)
      .subscribe((refinedPlaylists) => {
        this.currentCountryPlaylists = refinedPlaylists;
        for (const currentPlaylist of this.currentCountryPlaylists) {
          currentPlaylist.name = currentPlaylist.name.split('of ')[1];
          currentPlaylist.name = currentPlaylist.name.split(this.currentCountryCode)[0];
        }
      });


  }


}
