import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';


@Injectable
export class RefinedAlbumService {
  baseUrl: string;

  constructor(private http: HttpClient) {
    this.baseUrl = '/api/v1/albums';
  }

  getFromArtistId(artistId: string) {
    return this.http.get(this.baseUrl + '/from-artist-id/' + artistId);
  }

}
