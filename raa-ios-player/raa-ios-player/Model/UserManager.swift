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

class UserManager : NSObject, CLLocationManagerDelegate {
    static let REGISTER_ENDPOINT = Context.API_URL_PREFIX + "/registerDevice/iOS"

    public let user = User()
    
    private var locationManager = CLLocationManager()
    private var geocoder = CLGeocoder()
    private var jsonEncoder = JSONEncoder()

    
    struct PropertyKey {
        static let currentRegisteredId = "CurrentRegisteredId"
        static let currentRegisteredLocationString = "CurrentRegisteredLocation"
    }
    
    required override init() {
        super.init()
        
        self.initLocationManager()
        
        // This is the unique device id we register in server (a generated UUID string)
        self.user.Id = Context.Instance.settings.string(forKey: PropertyKey.currentRegisteredId) ?? UUID().uuidString
        self.user.TimeZone = NSTimeZone.local.identifier
        
        self.locateDevice()
    }
    
    private func registerDevice() {
        // Cases in which we (re)register the device
        // 1- If not registered before (no matter what)
        // 2- If device location is changed (and we know it -> LocationString is not empty)
        if (Context.Instance.settings.string(forKey: PropertyKey.currentRegisteredId) == nil ||
            (self.user.LocationString != "//" && Context.Instance.settings.string(forKey: PropertyKey.currentRegisteredLocationString) != self.user.LocationString)) {

            var request = URLRequest(url: URL(string: UserManager.REGISTER_ENDPOINT)!)
            request.httpMethod = "POST"
            request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            request.addValue("application/json", forHTTPHeaderField: "Accept")
            request.httpBody = try! self.jsonEncoder.encode(self.user)
            
            let task = URLSession.shared.dataTask(with: request) { data, response, error in
                guard error == nil else {
                    os_log("Error while registering device: %@", type: .error, error!.localizedDescription)
                    return
                }
                os_log("Registered device successfully!", type: .default)
                
                // Save the values in settings file
                Context.Instance.settings.set(self.user.Id, forKey: PropertyKey.currentRegisteredId)
                Context.Instance.settings.set(self.user.LocationString, forKey: PropertyKey.currentRegisteredLocationString)
            }
            task.resume()
        }
        else {
            os_log("Device location has not changed or not accessible, don't bother re-registering with server", type: .default)
        }
    }

    
    private func initLocationManager() {
        self.locationManager.delegate = self
        self.locationManager.desiredAccuracy = kCLLocationAccuracyThreeKilometers
        self.locationManager.requestWhenInUseAuthorization()
    }
    
    private func locateDevice() {
        if CLLocationManager.authorizationStatus() == CLAuthorizationStatus.authorizedWhenInUse && CLLocationManager.locationServicesEnabled() {
            // Async, callback will invoked
            self.locationManager.requestLocation()
        } else {
            if CLLocationManager.authorizationStatus() != .notDetermined {
                os_log("Device location not obtainable, proceeding with device registering", type: .default)
                registerDevice()
            }
            // Otherwise we wait to device location to come in
        }
    }
    
    internal func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        if status == CLAuthorizationStatus.authorizedWhenInUse {
            locateDevice();
        }
    }
    
    internal func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if locations.last != nil {
            self.user.Latitude = locations.last!.coordinate.latitude
            self.user.Longitude = locations.last!.coordinate.longitude
            self.geocoder.reverseGeocodeLocation(locations.last!) {
                placemarks, error in
                if placemarks?.last != nil {
                    self.user.Country = placemarks?.last!.country
                    self.user.State = placemarks?.last!.administrativeArea
                    self.user.City = placemarks?.last!.locality
                }
                // Even if we cannot reverse geocode, we should register device
                self.registerDevice()
            }
        }
    }

    internal func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        self.locationManager.stopUpdatingLocation()
        os_log("Error while obtaining device location %@", type: .error, error.localizedDescription)
        // Continue with registering process
        self.registerDevice()
    }
}

