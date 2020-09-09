import {Injectable} from '@angular/core';
import {environment} from '../environments/environment';
import {Subject} from 'rxjs';
import { v4 as randomUUID } from 'uuid';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  websocket: WebSocket;
  online = new Subject<boolean>();
  responseString = new Subject<JSON>();
  responseBinary = new Subject<Blob>();

  constructor() {
    this.online.subscribe(i => {
      console.log(i ? 'conntected' : 'disconnected');
    });
  }

  connect(url: string = environment.uri): void {
    this.websocket = new WebSocket(url);
    this.websocket.onopen = () => this.online.next(true);
    this.websocket.onclose = () => this.online.next(false);
    this.websocket.onmessage = (evt: MessageEvent) => {
      console.log(evt.type);
      if (evt.data instanceof Blob) {
        console.log(evt.data);
        this.responseBinary.next(evt.data);
      } else {
        console.log(JSON.parse(evt.data));
        this.responseString.next(JSON.parse(evt.data));
      }
    };
    this.websocket.onerror = (e) => console.log(e);
  }

  /*
{
"tag": "json",
"endpoint": "file/get",
"data": {
"name": "test",
"password": "#Test123",
"file_end": "txt",
"material_uuid": "75fc9ad2-8e97-4c7c-9df4-ea9a8781afbe"
}}
   */




  requestOnly(req: JSON): void {
    this.websocket.send(JSON.stringify(req));
  }

  request(endpoint: string, data: JSON): void {
    const tag = randomUUID();
    const req = {
      tag,
      endpoint,
      data
    };
    this.websocket.send(JSON.stringify(req));
  }

  sendFile(file: File): void{
    console.log(file.size / 65000 + ' parts');
    for (let i = 0; i < file.size; i += 65000) {
      const filePart: Blob = file.slice(i, i + 65000);
      this.websocket.send(filePart);
    }
    console.log('finished');
  }
}
