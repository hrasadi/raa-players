//
//  User.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class User : Codable {
    public var Id: String!
    
    // IP and DeviceType will be deduced in server-side
    public var TimeZone: String!
    
    // Location data
    public var Country: String! {
        didSet {
            if Country == nil {
                Country = ""
            }
        }
    }
    public var State: String! {
        didSet {
            if State == nil {
                State = ""
            }
        }
    }
    public var City: String! {
        didSet {
            if City == nil {
                City = ""
            }
        }
    }
    
    public var LocationString: String? {
        get {
            return self.Country + "/" + self.State + "/" + self.City
        }
    }
    
    public var Latitude: Double?
    public var Longitude: Double?
}
