//
//  LiveBroadcastManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/11/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation

class LiveBroadcastManager : UICommunicator {
    static let LIVE_LINEUP_URL = Context.LIVE_INFO_URL_PREFIX + "/live-lineup.json"
    static let LIVE_BROADCAST_STATUS_URL = Context.LIVE_INFO_URL_PREFIX + "/status.json"

    private var jsonDecoder = JSONDecoder()
    
    public var liveBroadcastStatus: LiveBroadcastStatus?
    public var liveLineup: [String : [CProgram]] = [:]
    public var flattenLiveLineup: [CProgram] = []
    
    override init() {
        super.init()
    }
    
    func initiate() {
        self.loadLiveLineup()
        self.loadBroadcastStatus() 
    }
    
    func loadLiveLineup() {
        let task = URLSession.shared.dataTask(with: URL(string: LiveBroadcastManager.LIVE_LINEUP_URL)!) {
            data, response, error in
            guard error == nil else {
                os_log("Error while loading live lineup: %@", type: .error, error!.localizedDescription)
                return
            }
            os_log("Fetched live lineup from server.", type: .default)
            
            guard data != nil else {
                return
            }
            
            self.liveLineup = try! self.jsonDecoder.decode(type(of: self.liveLineup), from: data!)
            self.flattenData()
            
            self.notifyModelUpdate()
        }
        task.resume()
    }
    
    private func flattenData() {
        self.flattenLiveLineup = []
        let sortedDates = Array(self.liveLineup.keys).sorted(by: <)
        for date in sortedDates {
            self.flattenLiveLineup += self.liveLineup[date] ?? []
        }
    }
    
    private func loadBroadcastStatus() {
        let task = URLSession.shared.dataTask(with: URL(string: LiveBroadcastManager.LIVE_BROADCAST_STATUS_URL)!) {
            data, response, error in
            guard error == nil else {
                os_log("Error while loading live status: %@", type: .error, error!.localizedDescription)
                return
            }
            os_log("Fetched live status from server.", type: .default)
            
            guard data != nil else {
                return
            }
            
            self.liveBroadcastStatus = try! self.jsonDecoder.decode(LiveBroadcastStatus.self, from: data!)
            
            self.notifyModelUpdate()
        }
        task.resume()
    }
    
    func getMostRecentProgramIndex() -> Int? {
        return self.flattenLiveLineup.index { (program) -> Bool in
            return program.CanonicalIdPath == self.liveBroadcastStatus?.MostRecentProgram
        }
    }
    
    func isProgramOver(programIndex: Int) -> Bool {
        if self.liveBroadcastStatus == nil {
            return false
        }
        
        let mostRecentProgramIndex = getMostRecentProgramIndex()            
        if mostRecentProgramIndex != nil {
            if programIndex < mostRecentProgramIndex! {
                return true
            }
            if programIndex == mostRecentProgramIndex! && self.liveBroadcastStatus?.IsCurrentlyPlaying == false {
                // The most recent program is over if radio is not playing right now
                return true
            }
        }
        return false
    }
    
    override func pullData() -> Any? {
        return self.flattenLiveLineup
    }
}

