//
//  FeedManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation
import PromiseKit

class FeedManager : UICommunicator<FeedData> {
    static let PUBLIC_FEED_ENDPOINT = Context.API_URL_PREFIX + "/publicFeed"
    static let PERSONAL_FEED_ENDPOINT = Context.API_URL_PREFIX + "/personalFeed"

    private var jsonDecoder = JSONDecoder()

    public var feedData = FeedData()
    private var feedDataResolver: Resolver<FeedData>?
    
    private var isLoading = false
    
    private var publicFeedRefreshTimer: Timer?
    private var personalFeedRefreshTimer: Timer?

    func initiate() {
        self.isLoading = true

        firstly {
            when(resolved: self.loadPublicFeed(), self.loadPersonalFeed())
        }.done { _ -> Void in
            self.isLoading = false
            self.feedDataResolver?.resolve(self.feedData, nil)
            self.feedDataResolver = nil
            
            self.initiateRefereshTimers()
        }.catch { error in
            os_log("Error while downloading feeds, error is %@", type: .error, error.localizedDescription)
            self.feedDataResolver?.reject(error)
            self.feedDataResolver = nil
        }
    }
    
    // Load feed from server
    func loadPublicFeed() -> Promise<Bool> {
        return
            firstly {
                URLSession.shared.dataTask(.promise, with: URL(string: FeedManager.PUBLIC_FEED_ENDPOINT)!)
            }.flatMap { data, response in
                os_log("Fetched public feed from server.", type: .default)
                self.feedData.publicFeed = try! self.jsonDecoder.decode([PublicFeedEntry].self, from: data)
                self.feedData.publicFeed?.sort(by: {
                    (a, b) in
                    if (a.ReleaseTimestamp == nil || b.ReleaseTimestamp == nil) {
                        return true // No specific order
                    }
                    if a.ReleaseTimestamp! < b.ReleaseTimestamp! {
                        return true
                    }
                    return false
                })
                return true
            }
    }

    // Load feed from server
    func loadPersonalFeed() -> Promise<Bool> {
        let pUrlString = FeedManager.PERSONAL_FEED_ENDPOINT + "/" + Context.Instance.userManager.user.Id
        return
            firstly {
                URLSession.shared.dataTask(.promise, with: URL(string: pUrlString)!)
            }.flatMap{ data, response in
                os_log("Fetched personal feed from server.", type: .default)
                self.feedData.personalFeed = try! self.jsonDecoder.decode([PersonalFeedEntry].self, from: data)
                self.feedData.personalFeed?.sort(by: {
                    (a, b) in
                    if (a.ReleaseTimestamp == nil || b.ReleaseTimestamp == nil) {
                        return true // No specific order
                    }
                    if a.ReleaseTimestamp! < b.ReleaseTimestamp! {
                        return true
                    }
                    return false
                })
                return true
            }
    }
    
    func lookupPublicFeedEntry(_ publicFeedEntryId: String) -> PublicFeedEntry? {
        if self.feedData.publicFeed == nil {
            return nil
        }
        for entry in self.feedData.publicFeed! {
            if entry.Id == publicFeedEntryId {
                return entry
            }
        }
        return nil
    }

    func lookupPersonalFeedEntry(_ personalFeedEntryId: String) -> PersonalFeedEntry? {
        if self.feedData.personalFeed == nil {
            return nil
        }
        
        for entry in self.feedData.personalFeed! {
            if entry.Id == personalFeedEntryId {
                return entry
            }
        }
        return nil
    }

    func initiateRefereshTimers() {
        // Every 5 minutes
        self.publicFeedRefreshTimer = Timer.scheduledTimer(withTimeInterval: 300, repeats: true) { _ in
            firstly {
                self.loadPublicFeed()
            }.done { _ in
                self.notifyModelUpdate(data: self.feedData)
            }.catch({ error in
                os_log("Error while reloading public feed status %@", type:.error, error.localizedDescription)
            })
        }
        // Every minute
        self.publicFeedRefreshTimer = Timer.scheduledTimer(withTimeInterval: 60, repeats: true) { _ in
            firstly {
                self.loadPersonalFeed()
            }.done { _ in
                self.notifyModelUpdate(data: self.feedData)
            }.catch({ error in
                os_log("Error while reloading personal feed status %@", type:.error, error.localizedDescription)
            })
        }
    }
    override func pullData() -> Promise<FeedData> {
        return Promise { seal in
            if !self.isLoading {
                seal.resolve(self.feedData, nil)
            } else {
                print("Not Resolved in place")
                // This will be resolved later
                self.feedDataResolver = seal
            }
        }
    }
}

struct FeedData {
    public var publicFeed: [PublicFeedEntry]?
    public var personalFeed: [PersonalFeedEntry]?
}


