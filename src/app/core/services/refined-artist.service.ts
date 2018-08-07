import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LoggerService} from './logger.service';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs/internal/Observable';
import {RefinedArtist} from '../models';
import {catchError, tap} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class RefinedArtistService {
  constructor(private http: HttpClient,
              private logger: LoggerService) {
  }

  private url = environment.api_url + '/artists';

  getById(ids: string[]): Observable<RefinedArtist[]> {
    return this.http.get<RefinedArtist[]>(this.url + '/' + ids.toString().replace(new RegExp(',', 'g'), '&') + '/multi')
        .pipe(
          tap(refinedArtist => this.logger.info(`fetched Artists with id=${refinedArtist.toString()}`)),
          catchError(this.logger.error(`failed to get Artist from id`))
        );

  }

}

