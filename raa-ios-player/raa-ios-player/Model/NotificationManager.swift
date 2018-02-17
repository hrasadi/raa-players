//
//  NotificationManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/15/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation
import UserNotifications
import LocalAuthentication
import UIKit
import PromiseKit

class NotificationManager : NSObject {
    let notificationCenter = UNUserNotificationCenter.current()
    
    public var authorizedForPushNotification = false
    public var deviceToken: String?
    
    public var requestNotificationAuthorizationPromiseResolver: Resolver<Bool>?
    
    override init() {
        super.init()
    }
    
    func initiate() {
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
    
    public func requestNotificationAuthorization() -> Promise<Bool> {
        return Promise<Bool> { seal in
            UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { (granted, error) in
                // Enable or disable features based on authorization.
                if !granted {
                    self.authorizedForPushNotification = false
                    os_log("Permission not granted to show notifications")
                    seal.resolve(false, error)
                } else {
                    self.authorizedForPushNotification = true
                    // Register for push notifications
                    DispatchQueue.main.async() {
                        UIApplication.shared.registerForRemoteNotifications()
                    }
                    // NOTE It will be resolved from didRegisterForRemoteNotificationsWithDeviceToken in AppDelegate
                    self.requestNotificationAuthorizationPromiseResolver = seal
                }
            }
        }
    }
}

extension NotificationManager: UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let actionIdentifier = response.actionIdentifier
        
        switch actionIdentifier {
        case "LISTEN_ACTION":
//            Settings.backgroundTask = UIApplication.shared.beginBackgroundTask(withName: "RaaNotificationDelegateBackgroundTask") {
//                // When the task is about to terminate, we should hand resources back
//                UIApplication.shared.endBackgroundTask(Settings.backgroundTask)
//                Settings.backgroundTask = UIBackgroundTaskInvalid;
//            }
//            do {
//                Settings.getPlaybackManager().loadStatus()
//                try Settings.getPlaybackManager().audioSession.setActive(true)
//                Settings.getPlaybackManager().play()
//            } catch let e {
//                NSLog("Error happened while starting playback: " + e.localizedDescription)
//            }
            break
        default:
            break
        }
        
        completionHandler()
    }
}
