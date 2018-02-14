//
//  FeedManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation

class FeedManager : UICommunicator {
    static let PUBLIC_FEED_ENDPOINT = Context.API_URL_PREFIX + "/publicFeed"
    static let PERSONAL_FEED_ENDPOINT = Context.API_URL_PREFIX + "/personalFeed"

    private var jsonDecoder = JSONDecoder()
    
    public var publicFeed: [PublicFeedEntry]?
    public var personalFeed: [PersonalFeedEntry]?

    override init() {
        super.init()
        
        self.loadPublicFeed()
        self.loadPersonalFeed()
    }
    
    // Load feed from server
    func loadPublicFeed() {
        let task = URLSession.shared.dataTask(with: URL(string: FeedManager.PUBLIC_FEED_ENDPOINT)!) {
            data, response, error in
            guard error == nil else {
                os_log("Error while loading public feed: %@", type: .error, error!.localizedDescription)
                return
            }
            os_log("Fetched public feed from server.", type: .default)

            guard data != nil else {
                return
            }
            
            self.publicFeed = try! self.jsonDecoder.decode([PublicFeedEntry].self, from: data!)
            self.publicFeed?.sort(by: {
            (a, b) in
                if (a.ReleaseTimestamp == nil || b.ReleaseTimestamp == nil) {
                    return true // No specific order
                }
                if a.ReleaseTimestamp! < b.ReleaseTimestamp! {
                    return true
                }
                return false
            })
            
            self.notifyModelUpdate()
        }
        task.resume()
    }

    // Load feed from server
    func loadPersonalFeed() {
        let pUrlString = FeedManager.PERSONAL_FEED_ENDPOINT + "/" + Context.Instance.userManager.user.Id
        let task = URLSession.shared.dataTask(with: URL(string: pUrlString)!) {
            data, response, error in
            guard error == nil else {
                os_log("Error while loading personal feed: %@", type: .error, error!.localizedDescription)
                return
            }
            os_log("Fetched personal feed from server.", type: .default)
            
            guard data != nil else {
                return
            }
            
            self.personalFeed = try! self.jsonDecoder.decode([PersonalFeedEntry].self, from: data!)
            self.personalFeed?.sort(by: {
                (a, b) in
                if (a.ReleaseTimestamp == nil || b.ReleaseTimestamp == nil) {
                    return true // No specific order
                }
                if a.ReleaseTimestamp! < b.ReleaseTimestamp! {
                    return true
                }
                return false
            })

            self.notifyModelUpdate()
        }
        task.resume()
    }
    
    func lookupPublicFeedEntry(_ publicFeedEntryId: String) -> PublicFeedEntry? {
        if self.publicFeed == nil {
            return nil
        }
        
        for entry in self.publicFeed! {
            if entry.Id == publicFeedEntryId {
                return entry
            }
        }
        return nil
    }

    func lookupPersonalFeedEntry(_ personalFeedEntryId: String) -> PersonalFeedEntry? {
        if self.personalFeed == nil {
            return nil
        }
        
        for entry in self.personalFeed! {
            if entry.Id == personalFeedEntryId {
                return entry
            }
        }
        return nil
    }

    override func pullData() -> Any? {
        return (self.publicFeed, self.personalFeed)
    }
}

