//
//  ProgramCard.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

@IBDesignable
class ProgramCard : Card {
    var blurV = UIVisualEffectView()
    var vibrancyV = UIVisualEffectView()

    private var programTitleLbl = UILabel()
    private var programSubtitleLbl = UILabel()
    
    private var timeTitle1Lbl = UILabel()
    private var timeValue1Lbl = UILabel()
    private var timeSubValue1Lbl = UILabel()
    private var timeTitle2Lbl = UILabel()
    private var timeValue2Lbl = UILabel()
    private var timeSubValue2Lbl = UILabel()

    public var programTitle: String = "برنامه" {
        didSet {
            programTitleLbl.text = programTitle
        }
    }

    public var programSubtitle: String = "توضیحات برنامه" {
        didSet {
            programSubtitleLbl.text = programSubtitle
        }
    }
    
    @IBInspectable public var timeTitle1: String = "از" {
        didSet {
            timeTitle1Lbl.text = timeTitle1
        }
    }
    
    public var timeValue1: String = "-" {
        didSet {
            timeValue1Lbl.text = timeValue1
        }
    }

    public var timeSubValue1: String = "" {
        didSet {
            timeSubValue1Lbl.text = timeSubValue1
        }
    }
    
    @IBInspectable public var timeTitle2: String = "تا" {
        didSet {
            timeTitle2Lbl.text = timeTitle2
        }
    }

    public var timeValue2: String = "-" {
        didSet {
            timeValue2Lbl.text = timeValue2
        }
    }
    
    public var timeSubValue2: String = "" {
        didSet {
            timeSubValue2Lbl.text = timeSubValue2
        }
    }
    
    @IBInspectable public var programTitleSize: CGFloat = 16
    @IBInspectable public var programSubtitleSize: CGFloat = 13

    @IBInspectable public var timeTitleSize: CGFloat = 12
    @IBInspectable public var timeValueSize: CGFloat = 20
    @IBInspectable public var timeSubValueSize: CGFloat = 10

    @IBInspectable public var blurEffect: UIBlurEffectStyle = .extraLight {
        didSet{
            blurV.effect = UIBlurEffect(style: blurEffect)
        }
    }

    override public init(frame: CGRect) {
        super.init(frame: frame)
        initialize()
    }
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        initialize()
    }
    
    override func initialize() {
        super.initialize()
        
        backgroundIV.addSubview(programTitleLbl);
        backgroundIV.addSubview(programSubtitleLbl);
        
        vibrancyV = UIVisualEffectView(effect: UIVibrancyEffect(blurEffect: UIBlurEffect(style: blurEffect)))
        backgroundIV.addSubview(blurV)
        blurV.contentView.addSubview(vibrancyV)
        blurV.contentView.addSubview(timeTitle1Lbl)
        blurV.contentView.addSubview(timeTitle2Lbl)
        blurV.contentView.addSubview(timeValue1Lbl)
        blurV.contentView.addSubview(timeValue2Lbl)
        blurV.contentView.addSubview(timeSubValue1Lbl)
        blurV.contentView.addSubview(timeSubValue2Lbl)
    }
    
    override open func draw(_ rect: CGRect) {
        super.draw(rect)

        // Main section
        programTitleLbl.text = programTitle
        programTitleLbl.textColor = textColor
        programTitleLbl.font = UIFont.systemFont(ofSize: programTitleSize, weight: .medium)
        //programTitleLbl.lineHeight(0.70)
        programTitleLbl.adjustsFontSizeToFitWidth = true
        programTitleLbl.minimumScaleFactor = 0.1
        programTitleLbl.lineBreakMode = .byTruncatingTail
        programTitleLbl.numberOfLines = 1
        backgroundIV.bringSubview(toFront: programTitleLbl)

        programSubtitleLbl.text = programSubtitle
        programSubtitleLbl.textColor = textColor
        programSubtitleLbl.font = UIFont.systemFont(ofSize: programSubtitleSize, weight: .light)
        programSubtitleLbl.lineHeight(0.70)
        programSubtitleLbl.adjustsFontSizeToFitWidth = true
        programSubtitleLbl.minimumScaleFactor = 0.1
        programSubtitleLbl.lineBreakMode = .byTruncatingTail
        programSubtitleLbl.numberOfLines = 1
        backgroundIV.bringSubview(toFront: programSubtitleLbl)

        
        let blur = UIBlurEffect(style: blurEffect)
        blurV.effect = blur
        
        // LINE 1
        timeTitle1Lbl.text = timeTitle1
        timeTitle1Lbl.textColor = textColor
        timeTitle1Lbl.alpha = CGFloat(0.3)
        timeTitle1Lbl.font = UIFont.systemFont(ofSize: timeTitleSize, weight: .medium)
        timeTitle1Lbl.lineHeight(0.70)
        timeTitle1Lbl.adjustsFontSizeToFitWidth = true
        timeTitle1Lbl.minimumScaleFactor = 0.1
        timeTitle1Lbl.lineBreakMode = .byTruncatingTail
        timeTitle1Lbl.numberOfLines = 1
        blurV.contentView.bringSubview(toFront: timeTitle1Lbl)

        timeValue1Lbl.text = timeValue1
        timeValue1Lbl.textColor = textColor
        timeValue1Lbl.font = UIFont.systemFont(ofSize: timeValueSize, weight: .light)
        timeValue1Lbl.adjustsFontSizeToFitWidth = true
        timeValue1Lbl.minimumScaleFactor = 0.1
        timeValue1Lbl.lineBreakMode = .byTruncatingTail
        timeValue1Lbl.numberOfLines = 1
        blurV.contentView.bringSubview(toFront: timeValue1Lbl)

        timeSubValue1Lbl.text = timeSubValue1
        timeSubValue1Lbl.textColor = textColor
        timeSubValue1Lbl.font = UIFont.systemFont(ofSize: timeSubValueSize, weight: .medium)
        timeSubValue1Lbl.adjustsFontSizeToFitWidth = true
        timeSubValue1Lbl.minimumScaleFactor = 0.1
        timeSubValue1Lbl.lineBreakMode = .byTruncatingTail
        timeSubValue1Lbl.numberOfLines = 1
        blurV.contentView.bringSubview(toFront: timeSubValue1Lbl)

        // LINE 2
        timeTitle2Lbl.text = timeTitle2
        timeTitle2Lbl.textColor = textColor
        timeTitle2Lbl.alpha = CGFloat(0.3)
        timeTitle2Lbl.font = UIFont.systemFont(ofSize: timeTitleSize, weight: .medium)
        timeTitle2Lbl.lineHeight(0.70)
        timeTitle2Lbl.adjustsFontSizeToFitWidth = true
        timeTitle2Lbl.minimumScaleFactor = 0.1
        timeTitle2Lbl.lineBreakMode = .byTruncatingTail
        timeTitle2Lbl.numberOfLines = 1
        blurV.contentView.bringSubview(toFront: timeTitle2Lbl)

        timeValue2Lbl.text = timeValue2
        timeValue2Lbl.textColor = textColor
        timeValue2Lbl.font = UIFont.systemFont(ofSize: timeValueSize, weight: .light)
        timeValue2Lbl.adjustsFontSizeToFitWidth = true
        timeValue2Lbl.minimumScaleFactor = 0.1
        timeValue2Lbl.lineBreakMode = .byTruncatingTail
        timeValue2Lbl.numberOfLines = 1
        blurV.contentView.bringSubview(toFront: timeValue2Lbl)

        timeSubValue2Lbl.text = timeSubValue2
        timeSubValue2Lbl.textColor = textColor
        timeSubValue2Lbl.font = UIFont.systemFont(ofSize: timeSubValueSize, weight: .medium)
        timeSubValue2Lbl.adjustsFontSizeToFitWidth = true
        timeSubValue2Lbl.minimumScaleFactor = 0.1
        timeSubValue2Lbl.lineBreakMode = .byTruncatingTail
        timeSubValue2Lbl.numberOfLines = 1
        blurV.contentView.bringSubview(toFront: timeSubValue2Lbl)

        layout()
    }
    
    override func layout(animating: Bool = true) {
        super.layout(animating: animating)
        
        let gimme = LayoutHelper(rect: backgroundIV.bounds)
        
        programTitleLbl.frame.size = CGSize(width: gimme.X(70) - 30, height: 20)
        programTitleLbl.frame.origin = CGPoint(x: gimme.RevX(0, width: programTitleLbl.bounds.size.width) - 20, y: 30)
        
        programSubtitleLbl.sizeToFit()
        programSubtitleLbl.frame.origin = CGPoint(x: gimme.RevX(0, width: programSubtitleLbl.bounds.size.width) - 20, y: programTitleLbl.frame.maxY + 10)
        
        blurV.frame = CGRect(x: 0,
                             y: 0,
                             width: gimme.X(30),
                             height: backgroundIV.bounds.height)
        
        vibrancyV.frame = blurV.frame
        
        let blurVGimme = LayoutHelper(rect: blurV.bounds)

        timeValue2Lbl.sizeToFit()
        timeValue2Lbl.frame.origin = CGPoint(x: blurVGimme.RevX(0, width: timeValue2Lbl.bounds.size.width) - 10, y: gimme.RevY(0, height: timeValue2Lbl.bounds.size.height) - 10)

        timeSubValue2Lbl.sizeToFit()
        timeSubValue2Lbl.frame.origin = CGPoint(x: timeValue2Lbl.frame.origin.x - timeSubValue2Lbl.bounds.size.width, y: timeValue2Lbl.frame.origin.y)

        timeTitle2Lbl.sizeToFit()
        timeTitle2Lbl.frame.origin = CGPoint(x: blurVGimme.RevX(0, width: timeTitle2Lbl.bounds.size.width) - 10, y: timeValue2Lbl.frame.origin.y - timeTitle2Lbl.bounds.size.height)

        timeValue1Lbl.sizeToFit()
        timeValue1Lbl.frame.origin = CGPoint(x: blurVGimme.RevX(0, width: timeValue1Lbl.bounds.size.width) - 10, y: timeTitle2Lbl.frame.origin.y - timeValue1Lbl.bounds.size.height - 10)

        timeSubValue1Lbl.sizeToFit()
        timeSubValue1Lbl.frame.origin = CGPoint(x: timeValue1Lbl.frame.origin.x - timeSubValue1Lbl.bounds.size.width, y: timeValue1Lbl.frame.origin.y)

        timeTitle1Lbl.sizeToFit()
        timeTitle1Lbl.frame.origin = CGPoint(x: blurVGimme.RevX(0, width: timeTitle1Lbl.bounds.size.width) - 10, y: timeValue1Lbl.frame.origin.y - timeTitle1Lbl.bounds.size.height)
    }
}

class ProgramCardTableViewCell : UITableViewCell {
    @IBOutlet public var card: ProgramCard?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init(style aStyle: UITableViewCellStyle, reuseIdentifier rid: String?) {
        super.init(style: aStyle, reuseIdentifier: rid)
    }
}