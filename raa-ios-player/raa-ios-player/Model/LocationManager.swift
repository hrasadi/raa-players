//
//  LocationManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/16/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import PromiseKit

class LocationManager : NSObject {

    private var geocoder = CLGeocoder()

    required override init() {
        super.init()
    }

    func initiate() {
        
    }
    
    public func locateDevice() -> Promise<Bool> {
        return
            firstly {
                CLLocationManager.requestLocation()
            }.then { locations -> Promise<[CLPlacemark]> in
                Context.Instance.userManager.user.Latitude = locations.last!.coordinate.latitude
                Context.Instance.userManager.user.Longitude = locations.last!.coordinate.longitude
                
                return self.geocoder.reverseGeocode(location: locations.last!)
            }.flatMap { placemarks -> Bool in
                if placemarks.last != nil {
                    let previousLocationString = Context.Instance.userManager.user.LocationString
                    
                    Context.Instance.userManager.user.Country = placemarks.last!.country
                    Context.Instance.userManager.user.State = placemarks.last!.administrativeArea
                    Context.Instance.userManager.user.City = placemarks.last!.locality
                    
                    if (Context.Instance.userManager.user.LocationString != previousLocationString) {
                        // Mark user for re-registering
                        return true
                    }
                }
                return false
            }
    }
}
