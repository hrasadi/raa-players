//
//  NotificationManager.swift
//  raa-ios-player
//
//  Created by Hamid on 9/20/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import Foundation
import UserNotifications
import LocalAuthentication
import UIKit

class NotificationManager : NSObject {
    let notificationCenter = UNUserNotificationCenter.current()
    
    override init() {
        super.init()
        
        // Register notification categories and actions
        let listenAction = UNNotificationAction(identifier: "LISTEN_ACTION",
                                                title: "گوش می‌دهم",
                                                options: UNNotificationActionOptions(rawValue: 0))
        let generalCategory = UNNotificationCategory(identifier: "media.raa.general",
                                                     actions: [listenAction],
                                                     intentIdentifiers: [],
                                                     options: .customDismissAction)
        // Register the notification category
        notificationCenter.setNotificationCategories([generalCategory])
        
        notificationCenter.delegate = self
    }
    
}

extension NotificationManager: UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let actionIdentifier = response.actionIdentifier
        
        switch actionIdentifier {
        case "LISTEN_ACTION":
            Settings.backgroundTask = UIApplication.shared.beginBackgroundTask(withName: "RaaNotificationDelegateBackgroundTask") {
                // When the task is about to terminate, we should hand resources back
                UIApplication.shared.endBackgroundTask(Settings.backgroundTask)
                Settings.backgroundTask = UIBackgroundTaskInvalid;
            }
            do {
                Settings.getPlaybackManager().loadStatus()
                try Settings.getPlaybackManager().audioSession.setActive(true)
                Settings.getPlaybackManager().play()
            } catch let e {
                NSLog("Error happened while starting playback: " + e.localizedDescription)
            }
            break
        default:
            break
        }

        completionHandler()
    }
}
