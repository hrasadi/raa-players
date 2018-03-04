//
//  ArchiveManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/11/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation
import PromiseKit

class ArchiveManager : UICommunicator<ArchiveData> {
    static let ARCHIVE_DIRECTORY_URL = Context.ARCHIVE_URL_PREFIX + "/raa1-archive.json"
    
    private var jsonDecoder = JSONDecoder()
    
    public var archiveData = ArchiveData()
    private var archiveDataResolver: Resolver<ArchiveData>?
    
    private var archiveDirectoryRefreshTimer: Timer?
    
    func initiate() {
        firstly {
            self.loadArchiveDirectory()
        }.done { _ in
            self.archiveDataResolver?.resolve(self.archiveData, nil)
            self.archiveDataResolver = nil
            
            self.initiateRefereshTimers()
        }.catch { error in
            os_log("Error while downloading archive, error is %@", type: .error, error.localizedDescription)
            self.archiveDataResolver?.reject(error)
        }
    }
    
    func loadArchiveDirectory() -> Promise<Bool> {
        return
            firstly {
                URLSession.shared.dataTask(.promise, with: URL(string: ArchiveManager.ARCHIVE_DIRECTORY_URL)!)
            }.flatMap { data, response in
                os_log("Fetched archive directory from server.", type: .default)
                self.archiveData.archiveURLDirectory = try! self.jsonDecoder.decode(type(of: self.archiveData.archiveURLDirectory), from: data)
                
                return true
            }
    }
    
    func initiateRefereshTimers() {
        // Every 24 hours
        self.archiveDirectoryRefreshTimer = Timer.scheduledTimer(withTimeInterval: 86400, repeats: true) { _ in
            firstly {
                self.loadArchiveDirectory()
            }.done { _ in
                self.notifyModelUpdate(data: self.archiveData)
            }.catch({ error in
                os_log("Error while reloading archive directory %@", type:.error, error.localizedDescription)
            })
        }
    }
    
    // This method works syncronously
    public func loadProgramArchiveSync(_ programId: String) -> [ArchiveEntry] {
        if (self.archiveData.archiveURLDirectory![programId] == nil) {
            return [] // Should not happen
        }
        
        let programArchiveFileURLString = Context.ARCHIVE_URL_PREFIX + "/" + self.archiveData.archiveURLDirectory![programId]!
        let programArchiveData = try? Data(contentsOf: URL(string: programArchiveFileURLString)!)
        if (programArchiveData == nil) {
            return []
        }
        
        let programArchive: [String: [CProgram]] = try! self.jsonDecoder.decode([String: [CProgram]].self, from: programArchiveData!)

        return flattenArchive(archive: programArchive)
    }
    
    private func flattenArchive(archive: [String : [CProgram]]) -> [ArchiveEntry] {
        var flattenArchive: [ArchiveEntry] = []
        let sortedDates = Array(archive.keys).sorted(by: >)
        for date in sortedDates {
            for program in archive[date]! {
                let archiveEntry = ArchiveEntry()
                archiveEntry.Program = program
                archiveEntry.ReleaseDateString = date

                flattenArchive.append(archiveEntry)
            }
        }
        
        return flattenArchive
    }
    
    override func pullData() -> Promise<ArchiveData> {
        return Promise<ArchiveData> { seal in
            if (self.archiveData.archiveURLDirectory != nil) {
                seal.resolve(self.archiveData, nil)
            } else {
                // Someone else will resolve this
                self.archiveDataResolver = seal
            }
        }
    }
}

struct ArchiveData {
    public var archiveURLDirectory: [String: String]? = nil
}



