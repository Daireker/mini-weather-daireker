package com.example.daireker;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.daireker.miniweather.R;
import com.example.daireker.util.MyService;
import com.example.daireker.util.UpdateWidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class MiniWeatherWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        /*RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        views.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_qing);
        views.setTextViewText(R.id.widget_city_text, "N/A");
        views.setTextViewText(R.id.widget_climate_text, "N/A");
        views.setTextViewText(R.id.widget_date_text, "N/A");
        views.setTextViewText(R.id.widget_temperature_text, "N/A");*/

        // Instruct the widget manager to update the widget
        //appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        /*for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }*/
        Intent update = new Intent(context, UpdateWidgetService.class);
        context.startService(update);
        super.onEnabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        /*Intent update = new Intent(context, UpdateWidgetService.class);
        context.startService(update);
        super.onEnabled(context);*/
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Intent update = new Intent(context, UpdateWidgetService.class);
        context.stopService(update);
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
}

