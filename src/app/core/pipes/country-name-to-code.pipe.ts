import {Pipe, PipeTransform} from '@angular/core';
import {Countries} from '../models/countries.model';

@Pipe({
  name: 'nameToCode'
})
export class CountryNameToCodePipe implements PipeTransform {
  kvObjectArray = Countries.asObject;
  // @ts-ignore
  countryMap = new Map(this.kvObjectArray.map(i => [i.Name, i.Code]));

   transform(countryName: string) {
    return this.countryMap.get(countryName);
  }

}
