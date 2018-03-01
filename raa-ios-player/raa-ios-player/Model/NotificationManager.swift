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
    
    private var backgroundTask: UIBackgroundTaskIdentifier? = UIBackgroundTaskInvalid
    
    override init() {
        super.init()
    }
    
    func initiate() {
        // Register notification categories and actions
        let listenAction = UNNotificationAction(identifier: "LISTEN_ACTION",
                                                title: "گوش می‌دهم",
                                                options: UNNotificationActionOptions(rawValue: 0))
        let liveCategory = UNNotificationCategory(identifier: "media.raa.Live",
                                                     actions: [listenAction],
                                                     intentIdentifiers: [],
                                                     options: .customDismissAction)

        let publicCategory = UNNotificationCategory(identifier: "media.raa.Public",
                                                  actions: [],
                                                  intentIdentifiers: [],
                                                  options: .customDismissAction)

        let personalCategory = UNNotificationCategory(identifier: "media.raa.Personal",
                                                        actions: [listenAction],
                                                        intentIdentifiers: [],
                                                        options: .customDismissAction)

        // Register the notification category
        notificationCenter.setNotificationCategories([liveCategory, publicCategory, personalCategory])
        
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
        let category = response.notification.request.content.categoryIdentifier
        
        switch actionIdentifier {
        case "LISTEN_ACTION":
            UIApplication.shared.beginBackgroundTask() {
                // When the task is about to terminate, we should hand resources back
                do {
                    try Context.Instance.playbackManager.audioSession.setActive(true)

                    if category == "media.raa.Live" {
                        Context.Instance.playbackManager.playLiveBroadcast()
                    } else if category == "media.raa.Personal" {
                        // Play personal feed
                        let userInfo = response.notification.request.content.userInfo
                        if let aps = userInfo["aps"] as? NSDictionary {
                            if let feedEntryId = aps["feedEntryId"] as? String {
                                Context.Instance.playbackManager.playPersonalFeed(feedEntryId)
                            }
                        }
                    }
                } catch {
                    os_log("Error while deactivating audio session")
                }
            }
            break
        default:
            break
        }
        completionHandler()
    }
}
