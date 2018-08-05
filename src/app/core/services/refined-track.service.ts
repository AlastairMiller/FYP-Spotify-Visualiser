import {Injectable} from '@angular/core';
import {catchError, map, tap} from 'rxjs/operators';
import {RefinedTrack} from '../models';
import {HttpClient} from '@angular/common/http';
import {LoggerService} from './logger.service';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs/internal/Observable';
import {of} from 'rxjs/internal/observable/of';


@Injectable({providedIn: 'root'})
export class RefinedTrackService {
  constructor(private  http: HttpClient,
              private logger: LoggerService) {
  }

  private url = environment.api_url + '/tracks';

  getRandom(): Observable<RefinedTrack> {
    return this.http.get<RefinedTrack>(this.url + '/random')
      .pipe(
        tap(refinedTrack => this.logger.info(`fetched track id=${refinedTrack.id}`)),
        catchError(this.handleError<RefinedTrack>(`getRandom()`))
      );
  }


  getMostPopularCountry(trackId: string): Observable<String> {
    return this.http.get<string>(this.url + trackId + '/popular-country');
  }

  private handleError<T> (operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      this.logger.error(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
