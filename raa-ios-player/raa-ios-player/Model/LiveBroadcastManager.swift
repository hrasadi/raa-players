//
//  LiveBroadcastManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/11/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation
import PromiseKit

class LiveBroadcastManager : UICommunicator<LiveLineupData> {
    static let LIVE_LINEUP_URL = Context.LIVE_INFO_URL_PREFIX + "/live-lineup.json"
    static let LIVE_BROADCAST_STATUS_URL = Context.LIVE_INFO_URL_PREFIX + "/status.json"

    private var jsonDecoder = JSONDecoder()

    public var liveLineupData = LiveLineupData()
    private var liveLineupDataResolver: Resolver<LiveLineupData>?

    private var isLoading = false

    private var lineupBroadcastStatusRefreshTimer: Timer?
    private var liveLineupRefreshTimer: Timer?

    func initiate() {
        self.isLoading = true

        firstly {
            when(resolved: self.loadLiveLineup(), self.loadBroadcastStatus())
        }.done { _ in
            self.isLoading = false
            self.liveLineupDataResolver?.resolve(self.liveLineupData, nil)
            self.liveLineupDataResolver = nil
            
            self.initiateRefereshTimers()
        }.catch { error in
            self.isLoading = false

            os_log("Error while downloading live lineup, error is %@", type: .error, error.localizedDescription)
            self.liveLineupDataResolver?.reject(error)
        }
    }
    
    func loadLiveLineup() -> Promise<Bool> {
        return
            firstly {
                URLSession.shared.dataTask(.promise, with: URL(string: LiveBroadcastManager.LIVE_LINEUP_URL)!)
            }.flatMap { data, response in
                os_log("Fetched live lineup from server.", type: .default)
                self.liveLineupData.liveLineup = try! self.jsonDecoder.decode(type(of: self.liveLineupData.liveLineup), from: data)
                self.flattenData()

                return true
            }
    }
    
    private func flattenData() {
        self.liveLineupData.flattenLiveLineup = []
        let sortedDates = Array(self.liveLineupData.liveLineup.keys).sorted(by: <)
        for date in sortedDates {
            self.liveLineupData.flattenLiveLineup! += self.liveLineupData.liveLineup[date] ?? []
        }
    }
    
    private func loadBroadcastStatus() -> Promise<Bool> {
        return
            firstly {
              URLSession.shared.dataTask(.promise, with: URL(string: LiveBroadcastManager.LIVE_BROADCAST_STATUS_URL)!)
            }.flatMap { data, response in
                os_log("Fetched live status from server.", type: .default)
                self.liveLineupData.liveBroadcastStatus = try! self.jsonDecoder.decode(LiveBroadcastStatus.self, from: data)
                return true
            }
    }
    
    func getMostRecentProgramIndex() -> Int? {
        return self.liveLineupData.flattenLiveLineup?.index { (program) -> Bool in
            return program.CanonicalIdPath == self.liveLineupData.liveBroadcastStatus?.MostRecentProgram
        }
    }
    
    func isProgramOver(programIndex: Int) -> Bool {
        if self.liveLineupData.liveBroadcastStatus == nil {
            return false
        }
        
        let mostRecentProgramIndex = getMostRecentProgramIndex()            
        if mostRecentProgramIndex != nil {
            if programIndex < mostRecentProgramIndex! {
                return true
            }
            if programIndex == mostRecentProgramIndex! && self.liveLineupData.liveBroadcastStatus?.IsCurrentlyPlaying == false {
                // The most recent program is over if radio is not playing right now
                return true
            }
        }
        return false
    }
    
    func initiateRefereshTimers() {
        if self.lineupBroadcastStatusRefreshTimer != nil {
            self.lineupBroadcastStatusRefreshTimer?.invalidate()
        }
        // Every 20 seconds
        self.lineupBroadcastStatusRefreshTimer = Timer.scheduledTimer(withTimeInterval: 20, repeats: true) { _ in
            firstly {
                self.loadBroadcastStatus()
            }.done { _ in
                self.notifyModelUpdate(data: self.liveLineupData)
            }.catch({ error in
                os_log("Error while reloading live broadcast status %@", type:.error, error.localizedDescription)
            })
        }
        
        if self.liveLineupRefreshTimer != nil {
            self.liveLineupRefreshTimer?.invalidate()
        }
        // Every 2 hours
        self.liveLineupRefreshTimer = Timer.scheduledTimer(withTimeInterval: 7200, repeats: true) { _ in
            firstly {
                self.loadLiveLineup()
            }.done { _ in
                self.notifyModelUpdate(data: self.liveLineupData)
            }.catch({ error in
                os_log("Error while reloading live lineup %@", type:.error, error.localizedDescription)
            })
        }
    }
    
    override func pullData() -> Promise<LiveLineupData> {
        return Promise<LiveLineupData> { seal in
            if !self.isLoading {
                seal.resolve(self.liveLineupData, nil)
            } else {
                // Someone else will resolve this
                self.liveLineupDataResolver = seal
            }
        }
    }
}

struct LiveLineupData {
    public var liveBroadcastStatus: LiveBroadcastStatus?
    public var liveLineup: [String : [CProgram]] = [:]
    public var flattenLiveLineup: [CProgram]?
}


