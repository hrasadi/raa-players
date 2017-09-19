//
//  AppDelegate.swift
//  raa-ios-player
//
//  Created by Hamid on 9/7/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import UIKit
import UserNotifications

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, willFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey : Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { (granted, error) in
            // Enable or disable features based on authorization.
            if !granted {
                Settings.authorizedToSendNotification = false
                print("Permission not granted to show notifications")
            } else {
                Settings.authorizedToSendNotification = true
            }
        }
        
        // Register Notification delegates
        Settings.getPlaybackManager().registerNotificationDelegate()
        
        return true
    }
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {

        // We want to fetch status
        application.setMinimumBackgroundFetchInterval(30)

        return true
    }
    
    func application(_ application: UIApplication,
                     performFetchWithCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {

        // Check for status (with notifications turned on - if allowed by user)
        Settings.getPlaybackManager().loadStatus(Settings.getValue(Settings.NotifyNewProgramKey) ?? false)
        
        completionHandler(.newData)
    }
        
    func applicationWillResignActive(_ application: UIApplication) {
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        Settings.getPlaybackManager().deactivate()
        application.endReceivingRemoteControlEvents()
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // And receive media player commands
        UNUserNotificationCenter.current().getNotificationSettings() { ns in
            if ns.alertSetting == .enabled {
                Settings.authorizedToSendNotification = true
            } else {
                Settings.authorizedToSendNotification = false
            }
        }
        
        Settings.getPlaybackManager().activate()
    }

    func applicationWillTerminate(_ application: UIApplication) {
        application.endReceivingRemoteControlEvents()
    }
}

