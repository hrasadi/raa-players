//
//  AppDelegate.swift
//  raa-ios-player
//
//  Created by Hamid on 1/23/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import UIKit
import PromiseKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        UIView.appearance().semanticContentAttribute = UISemanticContentAttribute.forceRightToLeft;

        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {

        let previousNotificationToken = Context.Instance.userManager.user.NotificationToken
        
        // Convert token to string
        Context.Instance.userManager.user.NotificationToken = deviceToken.reduce("", {$0 + String(format: "%02X", $1)})

        // Resolve the registeration promise
        let shouldReregister = previousNotificationToken != Context.Instance.userManager.user.NotificationToken
        Context.Instance.userManager.notificationManager.requestNotificationAuthorizationPromiseResolver?.resolve(shouldReregister, nil as Error?)
        Context.Instance.userManager.notificationManager.requestNotificationAuthorizationPromiseResolver = nil
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        Context.Instance.userManager.notificationManager.requestNotificationAuthorizationPromiseResolver?.resolve(false, error)
        Context.Instance.userManager.notificationManager.requestNotificationAuthorizationPromiseResolver = nil
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // On phone call
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Todo
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
    }

    func applicationWillTerminate(_ application: UIApplication) {
    }


}

