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
    static let PINFO_DIR_ENDPOINT = Context.API_URL_PREFIX + "/programInfoDirectory"

    private var jsonDecoder = JSONDecoder()
    private var programInfoResolver: Resolver<ProgramInfoDirectory>?
    
    public var programInfoDirectory: ProgramInfoDirectory?
    
    override init() {
        super.init()
    }
    
    func initiate() {
        self.loadProgramInfoDirectory()
    }

    // Load feed from server
    func loadProgramInfoDirectory() {
        firstly {
            URLSession.shared.dataTask(.promise, with: URL(string: ProgramInfoDirectoryManager.PINFO_DIR_ENDPOINT)!)
        }.done { data, respose in
            os_log("Fetched ProgramInfo directory from server.", type: .default)
            self.programInfoDirectory = try! self.jsonDecoder.decode(ProgramInfoDirectory.self, from: data)
            if (self.programInfoResolver != nil) {
                self.programInfoResolver?.resolve(self.programInfoDirectory, nil)
                self.programInfoResolver = nil
            }
            self.notifyModelUpdate(data: self.programInfoDirectory!)
        }.catch { error in
            os_log("Error while loading program info directory: %@", type: .error, error.localizedDescription)
            if (self.programInfoResolver != nil) {
                self.programInfoResolver?.reject(error)
                self.programInfoResolver = nil
            }
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
            if (self.programInfoDirectory != nil) {
                seal.resolve(self.programInfoDirectory, nil)
            } else {
                // Someone else will resolve this
                self.programInfoResolver = seal
            }
        }
    }
}
