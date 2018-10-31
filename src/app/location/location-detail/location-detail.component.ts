import {Component, OnInit} from '@angular/core';
import {RefinedArtistService, RefinedPlaylistService, RefinedTrackService} from '../../core/services';
import {RefinedArtist, RefinedPlaylist, RefinedTrack} from '../../core/models';
import {ActivatedRoute} from '@angular/router';
import {CountryNameToCodePipe} from '../../core/pipes/country-name-to-code.pipe';
import {Chart} from 'chart.js';


@Component({
  selector: 'app-list-detail-page',
  templateUrl: './location-detail.component.html',
  styleUrls: ['./location-detail.component.css']
})
export class LocationDetailComponent implements OnInit {

  currentCountry: string;
  currentCountryCode: string;
  currentCountryPlaylists: RefinedPlaylist[];
  mostPopularTrackIdMaps: Map<string, number>;
  mostPopularGenreMaps: Map<string, number>;
  mostPopularTracksHere: RefinedTrack[];
  mostPopularArtistsHere: RefinedArtist[];
  private mostPopularGenresHere: string[];


  constructor(private playlistService: RefinedPlaylistService,
              private route: ActivatedRoute,
              private countryToCodePipe: CountryNameToCodePipe,
              private trackService: RefinedTrackService,
              private artistService: RefinedArtistService) {
  }

  ngOnInit(): void {

    this.currentCountry = this.route.snapshot.paramMap.get('locationName');
    this.currentCountryCode = this.countryToCodePipe.transform(this.currentCountry);
    this.playlistService.getPlaylistLocation(this.currentCountryCode, 0, 5)
      .subscribe((refinedPlaylists) => {
        this.currentCountryPlaylists = refinedPlaylists;
        for (const currentPlaylist of this.currentCountryPlaylists) {
          currentPlaylist.name = currentPlaylist.name.split('of ')[1];
          currentPlaylist.name = currentPlaylist.name.split(this.currentCountryCode)[0];
        }
      });

    this.playlistService.getPopularTracks(this.currentCountryCode, 0, 5)
      .subscribe((refinedTrackIdMap) => {
        this.mostPopularTrackIdMaps = refinedTrackIdMap;
        let formattedRequestString = 'null';

        for (const popularTrackId of refinedTrackIdMap) {
          if (formattedRequestString === 'null') {
            // @ts-ignore
            formattedRequestString = (popularTrackId._id);
          } else {
            // @ts-ignore
            formattedRequestString = formattedRequestString + '&' + popularTrackId._id;
          }
        }
        this.trackService.getMultiple(formattedRequestString)
          .subscribe((refinedTracks) => {
            this.mostPopularTracksHere = refinedTracks;
          });
      });
    this.playlistService.getPopularArtists(this.currentCountryCode, 0, 5)
      .subscribe((refinedArtistIdMap) => {
        let formattedRequestString = 'null';

        for (const popularArtistId of refinedArtistIdMap) {
          if (formattedRequestString === 'null') {
            // @ts-ignore
            formattedRequestString = popularArtistId._id;
          } else {
            // @ts-ignore
            formattedRequestString = formattedRequestString + '&' + popularArtistId._id;
          }
        }
        this.artistService.getMultiple(formattedRequestString)
          .subscribe((refinedArtists) => {
            this.mostPopularArtistsHere = refinedArtists;
          });
        this.playlistService.getPopularGenres(this.currentCountryCode, 0, 5)
          .subscribe((genreMap) => {
              this.mostPopularGenreMaps = genreMap;
            }
          );
      });


  }


}
