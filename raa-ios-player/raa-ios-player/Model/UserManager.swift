//
//  User.swift
//  raa-ios-player
//
//  Created by Hamid on 1/28/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation
import CoreLocation
import PromiseKit

class UserManager {
    static let REGISTER_ENDPOINT = Context.API_URL_PREFIX + "/registerDevice/iOS"

    private var jsonEncoder = JSONEncoder()
    private var jsonDecoder = JSONDecoder()
    public var locationManager = LocationManager()
    public var notificationManager = NotificationManager()

    public var user: User = User()
    
    struct PropertyKey {
        static var user = "User"
    }
    
    public func initiate() {
        self.loadUser()
        self.locationManager.initiate()
        self.notificationManager.initiate()
        
        firstly {
            when(resolved: self.locationManager.locateDevice(), self.notificationManager.requestNotificationAuthorization())
            }.done { result in
                if result[0] == Result<Bool>.fulfilled(true) || result[1] == Result<Bool>.fulfilled(true) {
                    // register if either token or locations got updated
                    self.registerUser()
                    Context.Instance.settings.set(try! self.jsonEncoder.encode(self.user), forKey: PropertyKey.user)
                }
            }.catch { error in
                os_log("Error while obtaining device location %@", type: .error, error.localizedDescription)
        }
    }

    private func registerUser() {
        // Cases in which we (re)register the device
        // 1- If not registered before (no matter what)
        // 2- If device location is changed (and we know it -> LocationString is not empty)
        var request = URLRequest(url: URL(string: UserManager.REGISTER_ENDPOINT)!)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        request.httpBody = try! self.jsonEncoder.encode(self.user)
        
        firstly {
            URLSession.shared.dataTask(.promise, with: request)
        }.done { response in
            os_log("Registered device successfully!", type: .default)
        }.catch { error in
            os_log("Error while registering device: %@", type: .error, error.localizedDescription)
        }
    }
    
    private func loadUser() {
        if Context.Instance.settings.object(forKey: PropertyKey.user) == nil {
            self.user = User()
            // This is the unique device id we register in server (a generated UUID string)
            self.user.Id = UUID().uuidString
            self.user.TimeZone = NSTimeZone.local.identifier
        } else {
            do {
                self.user = try self.jsonDecoder.decode(User.self, from: (Context.Instance.settings.object(forKey: PropertyKey.user) as! Data))
            } catch {
                os_log("Fatal: This data should not be corrupted. We really need to do something", type: .error)
                // Remove this user. Hopefully next time it will be fixed by reregistering
                Context.Instance.settings.removeObject(forKey: PropertyKey.user)
            }
        }
    }
}

extension Result where T == Bool {
    static public func ==(lhs: Result<Bool>, rhs: Result<Bool>) -> Bool {
        switch (lhs, rhs) {
        case let (.fulfilled(a), .fulfilled(b)):
            return a == b
        case (.rejected(_), .rejected(_)):
            return true
        default:
            return false
        }
    }
}

