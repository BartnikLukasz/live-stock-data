import {Component, computed, inject} from '@angular/core';
import {AnalyticsService} from '../../services/AnalyticsService';
import {CommonModule} from '@angular/common';
import {VwapChartComponent} from '../vwap-chart/vwap-chart';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, VwapChartComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private analytics = inject(AnalyticsService);

  symbol = computed(() => this.analytics.latestUpdate()?.symbol ?? 'Loading...');
  vwap = computed(() => this.analytics.latestUpdate()?.vwap ?? '0.00');
  volume = computed(() => this.analytics.latestUpdate()?.totalVolume ?? '0');
  count = computed(() => this.analytics.latestUpdate()?.count ?? 0);
}
