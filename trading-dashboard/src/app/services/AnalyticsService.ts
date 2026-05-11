import { Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {TradeAggregate} from '../models/TradeAggregate';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  public latestUpdate = signal<TradeAggregate | null>(null);
  private stompClient: Client;

  constructor() {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8060/ws-trading'),
      debug: (msg) => console.log(msg),
      reconnectDelay: 5000,
    });

    this.stompClient.onConnect = () => {
      this.stompClient.subscribe('/topic/analytics/BINANCE:BTCUSDT', (message) => {
        console.log('Received in Angular:', message.body);
        const data: TradeAggregate = JSON.parse(message.body);
        this.latestUpdate.set(data);
      });
    };

    this.stompClient.activate();
  }
}
