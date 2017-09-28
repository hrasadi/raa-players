//
//  AppDelegate.swift
//  raa-ios-player
//
//  Created by Hamid on 9/7/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import UIKit
import UserNotifications
import AVFoundation


@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, willFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey : Any]? = nil) -> Bool {
        
        _ = Settings.startup()
        
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { (granted, error) in
            // Enable or disable features based on authorization.
            if !granted {
                Settings.authorizedToSendNotification = false
                print("Permission not granted to show notifications")
            } else {
                // Register for push notifications
                DispatchQueue.main.async() {
                    UIApplication.shared.registerForRemoteNotifications()
                }               
                Settings.authorizedToSendNotification = true
            }
        }
        return true
    }
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {

        return true
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        // Convert token to string
        let deviceTokenString = deviceToken.reduce("", {$0 + String(format: "%02X", $1)})
        
        // Print it to console
        print("APNs device token: \(deviceTokenString)")
        
        // Persist it in your backend in case it's new
        let task = URLSession.shared.dataTask(with: URL(string: "https://api.raa.media/registerDevice/ios/" + deviceTokenString)!) { data, response, error in
            guard error == nil else {
                print("Error while registering APN device token: " + error!.localizedDescription)
                return
            }
            print("Registered APN token successfully!")
        }
        task.resume()
    }
    
    // Called when APNs failed to register the device for push notifications
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        // Print the error to console (you should alert the user that registration failed)
        print("APNs registration failed: \(error)")
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any]) {
        self.application(application, didReceiveRemoteNotification: userInfo) {_ in 
            // Do nothing!
        }
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // Do update the current status of program playback
        if let aps = userInfo["aps"] as? NSDictionary {
            if let alert = aps["alert"] as? NSString, alert.length > 0 {
                // Update the status, UPDATE: Do not query server, show what is sent down as part of
                // notification payload
                //Settings.getPlaybackManager().loadStatus()
                Settings.getPlaybackManager().currentProgram = userInfo["currentProgram"] as? String
                Settings.getPlaybackManager().currentClip = userInfo["currentClip"] as? String
            } else {
                // It's a silent notification. This means we need to stop playback
//                do {
//                    try Settings.getPlaybackManager().audioSession.setActive(true)
//                } catch _ {
//                }
                Settings.getPlaybackManager().stop()
                Settings.getPlaybackManager().unpopulateMediaInfoCenterNowPlaying()
            }
            
            // Any prior notification is no longer valid (it is outdated)
            UNUserNotificationCenter.current().removeAllDeliveredNotifications()
        }
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        // When user closes app window
        Settings.getPlaybackManager().deactivate()
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Each time we enter background, we reserve a task for use of notification delegates
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
        Settings.loadLineup()
        Settings.getPlaybackManager().activate()
    }

    func applicationWillTerminate(_ application: UIApplication) {
        Settings.getPlaybackManager().shutDown();
        application.endReceivingRemoteControlEvents()
    }
}

