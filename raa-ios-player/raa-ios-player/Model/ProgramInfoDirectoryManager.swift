//
//  ProgramInfoDirectoryManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/9/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation
import PromiseKit
import UIKit

class ProgramInfoDirectoryManager : UICommunicator<ProgramInfoDirectory> {
    private var jsonDecoder = JSONDecoder()
    private var programInfoResolver: Resolver<ProgramInfoDirectory>?
    
    public var programInfoDirectory: ProgramInfoDirectory?
    
    private var isLoading = false

    override init() {
        super.init()
    }
    
    func initiate() {
        self.loadProgramInfoDirectory()
    }

    // Load feed from server
    func loadProgramInfoDirectory() {
        self.isLoading = true
        
        firstly {
            when(resolved: self.loadProgramInfo(), self.loadArchiveDirectory())
        }.done { _ -> Void in
            self.isLoading = false
            self.programInfoResolver?.resolve(self.programInfoDirectory, nil)
            self.programInfoResolver = nil
        }.catch { error in
            self.isLoading = false
            
            os_log("Error while loading program info directory: %@", type: .error, error.localizedDescription)
            self.programInfoResolver?.reject(error)
            self.programInfoResolver = nil
        }
    }
    
    func loadProgramInfo() -> Promise<Bool> {
        let PINFO_DIR_ENDPOINT = Context.API_URL_PREFIX + "/programInfoDirectory"

        return
            firstly {
                URLSession.shared.dataTask(.promise, with: URL(string: PINFO_DIR_ENDPOINT)!)
            }.flatMap{ data, response in
                os_log("Fetched ProgramInfo directory from server.", type: .default)
                self.programInfoDirectory = try! self.jsonDecoder.decode(ProgramInfoDirectory.self, from: data)
                return true
            }
    }

    func loadArchiveDirectory() -> Promise<Bool> {
        let ARCHIVE_DIR_ENDPOINT = Context.ARCHIVE_URL_PREFIX + "/raa1-archive.json"

        return
            firstly {
                URLSession.shared.dataTask(.promise, with: URL(string: ARCHIVE_DIR_ENDPOINT)!)
            }.flatMap{ data, response in
                os_log("Fetched Archive directory from server.", type: .default)
                self.programInfoDirectory?.Archive = try! self.jsonDecoder.decode([String: String].self, from: data)
                return true
        }
    }
    
    public func preloadImages() -> Promise<Bool> {
        os_log("Preloading images from server...")

        return Promise<Bool> { seal in
            if self.programInfoDirectory != nil {
                for pinfo in self.programInfoDirectory!.ProgramInfos {
                    pinfo.value.thumbnailImageData = self.getImageData(pinfo.value.Thumbnail)
                    pinfo.value.bannerImageData = self.getImageData(pinfo.value.Banner)
                }
                seal.resolve(true, nil as Error?)
            }
        }
    }
    
    private func getImageData(_ urlString: String?) -> Data? {
        var result: Data? = nil

        if urlString != nil {
            let url = URL(string: urlString!)!
            let data = try? Data(contentsOf: url)
            if (data != nil) {
                result = data
            }
        }
        return result
    }
    
    override func pullData() -> Promise<ProgramInfoDirectory> {
        return Promise<ProgramInfoDirectory> { seal in
            if !self.isLoading {
                seal.resolve(self.programInfoDirectory, nil)
            } else {
                // Someone else will resolve this
                self.programInfoResolver = seal
            }
        }
    }
}
