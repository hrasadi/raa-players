//
//  User.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class User : Codable, Comparable {
    static func <(lhs: User, rhs: User) -> Bool {
        return lhs.Id < rhs.Id
    }
    
    static func ==(lhs: User, rhs: User) -> Bool {
        if (lhs.LocationString != rhs.LocationString) || (lhs.NotificationToken != rhs.NotificationToken) {
            return false
        }
        return true
    }

    public var Id: String!
    
    // IP and DeviceType will be deduced in server-side
    public var TimeZone: String!
    
    // Location data
    public var Country: String?
    public var State: String?
    public var City: String?
    
    public var LocationString: String? {
        get {
            let country = self.Country ?? ""
            let state = self.State ?? ""
            let city = self.City ?? ""

            return country + "/" + state + "/" + city
        }
    }
    
    public var Latitude: Double?
    public var Longitude: Double?
 
    public var NotificationToken: String?
    
    public var NotifyOnPersonalProgram: Int?
    public var NotifyOnPublicProgram: Int?
    public var NotifyOnLiveProgram: Int?
}
