import { Component } from '@angular/core';
import {WebsocketService} from './websocket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'oedu-test-frontend';
  websocket: WebsocketService = new WebsocketService();

  connect(): void {
    this.websocket.connect();
  }

  request(req: string): void {
    this.websocket.requestOnly(JSON.parse(req));
  }

  sendFile(files: File[]): void {
    for (const file of files) {
      this.websocket.sendFile(file);
    }
  }
}
