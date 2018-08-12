import {Injectable} from '@angular/core';
import {catchError, map, tap} from 'rxjs/operators';
import {RefinedPlaylist, RefinedTrack} from '../models';
import {HttpClient} from '@angular/common/http';
import {LoggerService} from './logger.service';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs/internal/Observable';


@Injectable({providedIn: 'root'})
export class RefinedTrackService {
  constructor(private  http: HttpClient,
              private logger: LoggerService) {
  }

  private url = environment.api_url + '/tracks';

  getRandom(): Observable<RefinedTrack> {
    return this.http.get<RefinedTrack>(this.url + '/random')
      .pipe(tap(refinedTrack => this.logger.info(`fetched track id=${refinedTrack.id}`)),);
  }


  getMostPopularCountry(trackId: string): Observable<String> {
    return this.http.get<string>(this.url + '/' + trackId + '/popular-country');
  }

  getMostPopular(offset: number, limit: number): Observable<RefinedTrack[]> {
    return this.http.get<RefinedTrack[]>(this.url + '/most-popular?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('fetched most popular Tracks')));

  }
}
