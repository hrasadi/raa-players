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
    static let PUBLIC_FEED_ENDPOINT = Context.SERVER_URL + "/publicfeed"

    private var jsonDecoder = JSONDecoder()
    
    public var publicFeed: [PublicFeedEntry]?
    public var personalFeed: [PersonalProgram]?

    override init() {
        super.init()
        
        self.loadPublicFeed()
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
            
            self.notifyModelUpdate()
        }
        task.resume()
    }
    
    override func pullData() -> Any? {
        return (self.publicFeed, self.personalFeed)
    }

}

